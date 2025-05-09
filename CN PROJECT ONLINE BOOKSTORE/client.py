import ssl
import json
import tkinter as tk
from tkinter import ttk, messagebox
from PIL import Image, ImageTk
import socket
import base64
import io
import os
import time

HOST = '10.20.203.81'  # Use localhost for testing
PORT = 12345

# Global variables
session_info = {
    "authenticated": False,
    "username": None,
    "session_id": None,
    "role": None
}

books = {}
cart = {}
cart_listbox = None
cart_frame = None
cart_visible = False
total_label = None

def recv_json(sock):
    try:
        # Read exactly 10 bytes for the length prefix
        length_data = sock.recv(10).decode().strip()
        if not length_data:
            return None
            
        total_length = int(length_data)
        received_data = b""
        
        # Keep receiving until we have all the data
        while len(received_data) < total_length:
            chunk = sock.recv(min(4096, total_length - len(received_data)))
            if not chunk:
                break
            received_data += chunk
            
        return json.loads(received_data.decode())
    except Exception as e:
        print(f"Error receiving data: {e}")
        return None

def send_request(sock, request):
    try:
        sock.sendall(request.encode())
        return True
    except Exception as e:
        print(f"Error sending request: {e}")
        return False

def fetch_books(client_socket):
    try:
        if send_request(client_socket, "FETCH"):
            return recv_json(client_socket)
        return {}
    except Exception as e:
        print(f"Error fetching books: {e}")
        return {}

def connect_to_server():
    try:
        # Create SSL context
        context = ssl.create_default_context()
        context.check_hostname = False
        context.verify_mode = ssl.CERT_NONE  # For self-signed cert

        # Create the socket
        raw_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket = context.wrap_socket(raw_sock, server_hostname=HOST)
        
        # Connect to server
        client_socket.connect((HOST, PORT))
        print(f"Connected to server at {HOST}:{PORT}")
        return client_socket
    except Exception as e:
        messagebox.showerror("Connection Error", f"Could not connect to server: {e}")
        return None

def login(username, password, client_socket, login_window):
    try:
        # Send authentication request
        auth_message = f"AUTH:{username}:{password}"
        if not send_request(client_socket, auth_message):
            messagebox.showerror("Login Error", "Failed to send authentication request")
            return
            
        # Wait for response
        response = recv_json(client_socket)
        if not response:
            messagebox.showerror("Login Error", "Failed to receive server response")
            return
        
        if response.get("status") == "auth_success":
            session_info["authenticated"] = True
            session_info["username"] = response.get("username")
            session_info["session_id"] = response.get("session_id")
            session_info["role"] = response.get("role")
            
            # Fetch books after login
            global books
            books = recv_json(client_socket)
            if not books:
                messagebox.showerror("Data Error", "Failed to receive book data")
                return

            # Close login window and open main bookstore
            login_window.destroy()
            create_bookstore_window(client_socket)
        else:
            messagebox.showerror("Login Failed", response.get("message", "Authentication failed"))
    except Exception as e:
        messagebox.showerror("Login Error", f"An error occurred: {e}")

def register(username, password, client_socket):
    try:
        if not username or not password:
            messagebox.showerror("Registration Error", "Username and password cannot be empty!")
            return
            
        register_message = f"REGISTER:{username}:{password}"
        if not send_request(client_socket, register_message):
            messagebox.showerror("Registration Error", "Failed to send registration request")
            return
            
        response = recv_json(client_socket)
        if not response:
            messagebox.showerror("Registration Error", "Failed to receive server response")
            return
        
        if response.get("status") == "register_success":
            messagebox.showinfo("Registration Successful", response.get("message", "Account created successfully. Please login."))
        else:
            messagebox.showerror("Registration Failed", response.get("message", "Failed to create account"))
    except Exception as e:
        messagebox.showerror("Registration Error", f"An error occurred: {e}")

def create_login_window():
    login_window = tk.Tk()
    login_window.title("📚 Online Bookstore - Login")
    login_window.geometry("400x500")
    login_window.configure(bg="#f0f0f0")
    
    # Try to connect to the server first
    client_socket = connect_to_server()
    if not client_socket:
        login_window.destroy()
        return
    
    # Header
    header_frame = tk.Frame(login_window, bg="#3f51b5", height=80)
    header_frame.pack(fill="x")
    
    title_label = tk.Label(
        header_frame,
        text="📚 Online Bookstore",
        font=("Helvetica", 20, "bold"),
        bg="#3f51b5",
        fg="white"
    )
    title_label.pack(pady=20)
    
    # Login form
    form_frame = tk.Frame(login_window, bg="#f0f0f0")
    form_frame.pack(pady=30)
    
    # Switch between login and register forms
    login_mode = tk.BooleanVar(value=True)
    
    def toggle_form():
        if login_mode.get():
            form_title.config(text="Login")
            submit_button.config(text="Login")
            switch_button.config(text="Need an account? Register")
        else:
            form_title.config(text="Register")
            submit_button.config(text="Register")
            switch_button.config(text="Already have an account? Login")
    
    form_title = tk.Label(
        form_frame,
        text="Login",
        font=("Helvetica", 16, "bold"),
        bg="#f0f0f0"
    )
    form_title.grid(row=0, column=0, columnspan=2, pady=10)
    
    # Username field
    tk.Label(form_frame, text="Username:", bg="#f0f0f0").grid(row=1, column=0, sticky="e", pady=5)
    username_entry = ttk.Entry(form_frame, width=25)
    username_entry.grid(row=1, column=1, pady=5)
    
    # Password field
    tk.Label(form_frame, text="Password:", bg="#f0f0f0").grid(row=2, column=0, sticky="e", pady=5)
    password_entry = ttk.Entry(form_frame, width=25, show="*")
    password_entry.grid(row=2, column=1, pady=5)
    
    # Submit button action
    def handle_submit():
        try:
            username = username_entry.get()
            password = password_entry.get()
            
            if not username or not password:
                messagebox.showerror("Error", "Username and password cannot be empty!")
                return
            
            if login_mode.get():
                login(username, password, client_socket, login_window)
            else:
                register(username, password, client_socket)
        except Exception as e:
            messagebox.showerror("Error", f"An error occurred: {e}")
    
    # Submit button
    submit_button = ttk.Button(form_frame, text="Login", command=handle_submit)
    submit_button.grid(row=3, column=0, columnspan=2, pady=20)
    
    # Switch between login/register
    def switch_mode():
        login_mode.set(not login_mode.get())
        toggle_form()
    
    switch_button = ttk.Button(form_frame, text="Need an account? Register", command=switch_mode)
    switch_button.grid(row=4, column=0, columnspan=2)
    
    login_window.mainloop()

def create_bookstore_window(client_socket):
    root = tk.Tk()
    root.title(f"📚 Online Bookstore - {session_info['username']}")
    root.geometry("1100x700")
    root.configure(bg="#f0f0f0")
    
    # Create header
    top_frame = tk.Frame(root, bg="#3f51b5")
    top_frame.pack(fill="x")
    title = tk.Label(top_frame, text="📚 Welcome to Online Bookstore!", font=("Helvetica", 24, "bold"), bg="#3f51b5", fg="white")

    title.pack(pady=10)
    
    # Add role indicator
    role_label = tk.Label(top_frame, text=f"Role: {session_info['role'].capitalize()}", font=("Helvetica", 12), bg="#3f51b5", fg="white")
    role_label.pack(pady=5)
    
    # Controls frame
    control_frame = tk.Frame(root, bg="#f0f0f0")
    control_frame.pack(pady=10)
    
    # Search field
    search_var = tk.StringVar()
    search_entry = ttk.Entry(control_frame, textvariable=search_var, width=30)
    search_entry.grid(row=0, column=0, padx=5)
    
    def search_books():
        query = search_var.get().strip()
        if not query:
            return
        send_request(client_socket, f"SEARCH:{query}")
        result = recv_json(client_socket)
        if result and result.get("status") == "found":
            display_books(result["books"])
            messagebox.showinfo("Search", f"Found {len(result['books'])} book(s)")
        else:
            messagebox.showinfo("Search", "No matches found.")
    
    ttk.Button(control_frame, text="Search", command=search_books).grid(row=0, column=1, padx=5)
    
    # Genre dropdown
    genre_var = tk.StringVar()
    genre_dropdown = ttk.Combobox(control_frame, textvariable=genre_var, state="readonly")
    genre_dropdown['values'] = list(set([book['genre'] for book in books.values()]))
    genre_dropdown.grid(row=0, column=2, padx=5)
    
    def genre_search():
        genre = genre_var.get()
        if not genre:
            return
        send_request(client_socket, f"GENRE:{genre}")
        genre_books = recv_json(client_socket)
        if genre_books:
            display_books(genre_books)
        else:
            messagebox.showinfo("Genre", "No books found in this genre.")
    
    ttk.Button(control_frame, text="Search Genre", command=genre_search).grid(row=0, column=3, padx=5)
    
    def fetch_top_books():
        send_request(client_socket, "TOP")
        top_data = recv_json(client_socket)
        if top_data:
            display_books(top_data)
        else:
            messagebox.showinfo("Top Books", "Could not fetch top books.")
    
    ttk.Button(control_frame, text="Top 3 Books", command=fetch_top_books).grid(row=0, column=4, padx=5)
    
    def reset_home():
        global books
        books = fetch_books(client_socket)
        if books:
            display_books(books)
            search_var.set("")
            genre_var.set("")
        else:
            messagebox.showerror("Error", "Could not fetch books data.")
    
    ttk.Button(control_frame, text="Home", command=reset_home).grid(row=0, column=5, padx=5)
    
    # Cart button
    def toggle_cart():
        global cart_frame, cart_visible
        if cart_visible:
            cart_frame.place_forget()
            cart_visible = False
        else:
            if not cart_frame:
                build_cart_frame()
            cart_frame.place(relx=0.75, rely=0.15, relwidth=0.23, relheight=0.75)
            cart_visible = True
    
    ttk.Button(control_frame, text="🛒 Cart", command=toggle_cart).grid(row=0, column=6, padx=5)
    
    # Logout button
    def logout():
        try:
            send_request(client_socket, "LOGOUT")
            response = recv_json(client_socket)
            if response and response.get("status") == "logout_success":
                root.destroy()
                client_socket.close()
                main()  # Restart the application with login screen
            else:
                messagebox.showerror("Logout Error", "Failed to logout properly.")
        except Exception as e:
            messagebox.showerror("Logout Error", f"An error occurred: {e}")
            root.destroy()
            main()  # Try to restart anyway
    
    ttk.Button(control_frame, text="Logout", command=logout).grid(row=0, column=7, padx=5)
    
    # Scrollable content frame setup
    content_canvas = tk.Canvas(root, bg="#f0f0f0")
    scrollbar = ttk.Scrollbar(root, orient="vertical", command=content_canvas.yview)
    scrollable_frame = tk.Frame(content_canvas, bg="#f0f0f0")
    
    scrollable_frame.bind(
        "<Configure>",
        lambda e: content_canvas.configure(
            scrollregion=content_canvas.bbox("all")
        )
    )
    
    content_canvas.create_window((0, 0), window=scrollable_frame, anchor="nw")
    content_canvas.configure(yscrollcommand=scrollbar.set)
    
    content_canvas.pack(side="left", fill="both", expand=True, padx=20, pady=10)
    scrollbar.pack(side="right", fill="y")
    
    def build_cart_frame():
        global cart_frame, cart_listbox, total_label
        cart_frame = tk.Frame(root, bg="#eeeeee", bd=2, relief="ridge")
        tk.Label(cart_frame, text="🛒 Cart", font=("Helvetica", 16, "bold"), bg="#eeeeee").pack(pady=10)
        
        cart_listbox = tk.Listbox(cart_frame, font=("Helvetica", 12))
        cart_listbox.pack(fill="both", expand=True, padx=5, pady=5)
        
        remove_btn = ttk.Button(cart_frame, text="Remove Selected", command=remove_selected_from_cart)
        remove_btn.pack(pady=5)
        
        total_label = tk.Label(cart_frame, text="Total: $0", font=("Helvetica", 14), bg="#eeeeee")
        total_label.pack(pady=5)
        
        ttk.Button(cart_frame, text="Checkout", command=checkout_cart).pack(pady=10)
    
    def update_cart_display():
        if cart_listbox:
            cart_listbox.delete(0, tk.END)
            total = 0
            for book_id, qty in cart.items():
                if book_id in books:
                    book = books[book_id]
                    cart_listbox.insert(tk.END, f"{book['title']} x {qty}")
                    try:
                        price = float(book['price'].replace('$', ''))
                        total += price * qty
                    except ValueError:
                        pass
            total_label.config(text=f"Total: ${total:.2f}")
    
    def add_to_cart(book_id):
        try:
            send_request(client_socket, f"CART_ADD:{book_id}")
            response = recv_json(client_socket)
            if response and response.get("status") == "added":
                cart[book_id] = cart.get(book_id, 0) + 1
                # Ensure the cart frame is built before updating
                if cart_frame is None:
                    build_cart_frame()
                update_cart_display()
                messagebox.showinfo("Added to Cart", f"{response['book']['title']} added to cart.")
            else:
                messagebox.showerror("Error", "Failed to add item to cart.")
        except Exception as e:
            messagebox.showerror("Cart Error", f"An error occurred: {e}")
    
    def remove_selected_from_cart():
        if not cart_listbox:
            return
        selection = cart_listbox.curselection()
        if not selection:
            messagebox.showinfo("Remove Item", "Select an item to remove.")
            return
        selected = cart_listbox.get(selection[0])
        book_title = selected.split(" x ")[0]
        
        for book_id, book in books.items():
            if book['title'] == book_title:
                if cart[book_id] > 1:
                    cart[book_id] -= 1
                else:
                    del cart[book_id]
                try:
                    send_request(client_socket, f"CART_REMOVE:{book_id}")
                    recv_json(client_socket)
                except Exception:
                    pass
                update_cart_display()
                break
    
    def checkout_cart():
        if not cart:
            messagebox.showinfo("Cart Empty", "Add books to cart before checking out.")
            return
        summary = ""
        for book_id, qty in cart.items():
            for _ in range(qty):
                try:
                    send_request(client_socket, f"ORDER:{book_id}")
                    response = recv_json(client_socket)
                    if response.get("status") == "success":
                        book = response["book"]
                        summary += f"✔ {book['title']} ordered (${book['price']})\n"
                    else:
                        summary += f"❌ {books[book_id]['title']}: {response.get('message')}\n"
                except Exception as e:
                    summary += f"❌ {books[book_id]['title']}: Error processing\n"
        messagebox.showinfo("🛍 Order Summary", summary)
        cart.clear()
        update_cart_display()
        reset_home()
        
    def display_books(book_data):
        for widget in scrollable_frame.winfo_children():
            widget.destroy()
        
        for book_id, book in book_data.items():
            frame = tk.Frame(scrollable_frame, bg="white", bd=1, relief="solid")
            frame.pack(fill="x", padx=10, pady=5)
            
            if book.get("image_data"):
                try:
                    image = Image.open(io.BytesIO(base64.b64decode(book["image_data"])))
                    image = image.resize((60, 90))
                    photo = ImageTk.PhotoImage(image)
                    label = tk.Label(frame, image=photo)
                    label.image = photo
                    label.pack(side="left", padx=10, pady=5)
                except Exception:
                    tk.Label(frame, text="[Image Error]", bg="white", width=10).pack(side="left", padx=10)
            else:
                tk.Label(frame, text="[No Image]", bg="white", width=10).pack(side="left", padx=10)
            
            info = f"Title: {book['title']}\nAuthor: {book['author']}\nPrice: {book['price']}\nGenre: {book['genre']}\nQuantity: {book['quantity']}"
            tk.Label(frame, text=info, font=("Helvetica", 11), bg="white", justify="left").pack(side="left", padx=10)
            
            btns = tk.Frame(frame, bg="white")
            btns.pack(side="right", padx=10)
            ttk.Button(btns, text="Add to Cart", command=lambda b=book_id: add_to_cart(b)).pack(pady=2)
            ttk.Button(btns, text="Order Now", command=lambda b=book_id: direct_order(b)).pack(pady=2)
            
    def direct_order(book_id):
        try:
            send_request(client_socket, f"ORDER:{book_id}")
            response = recv_json(client_socket)
            if response and response.get("status") == "success":
                book = response["book"]
                messagebox.showinfo("Order Placed", f"Successfully ordered '{book['title']}'")
                reset_home()  # Refresh the book list to show updated quantities
            else:
                messagebox.showerror("Order Failed", response.get("message", "Failed to place order"))
        except Exception as e:
            messagebox.showerror("Order Error", f"An error occurred: {e}")
    
    display_books(books)
    root.mainloop()
    client_socket.close()

def main():
    create_login_window()

if __name__ == "__main__":
    main()