import socket
import threading
import json
import base64
import os
import ssl
import hashlib
import time

BOOKS_FILE = "books.json"
USERS_FILE = "users.json"
ORDERS_FILE = "orders.json"
IMAGE_FOLDER = "images"
HOST = '127.0.0.1'
PORT = 12345

wishlist = {}
cart_log = []
active_sessions = {}

def ensure_users_file():
    if not os.path.exists(USERS_FILE):
        default_users = {
            "admin": {
                "password": hashlib.sha256("admin123".encode()).hexdigest(),
                "role": "admin"
            },
            "user1": {
                "password": hashlib.sha256("user123".encode()).hexdigest(),
                "role": "customer"
            }
        }
        with open(USERS_FILE, "w") as f:
            json.dump(default_users, f, indent=2)
        print("✅ Created default users file")
    return True

def load_users():
    ensure_users_file()
    with open(USERS_FILE, "r") as f:
        return json.load(f)

def save_users(users):
    with open(USERS_FILE, "w") as f:
        json.dump(users, f, indent=2)

def load_books_with_images():
    try:
        with open(BOOKS_FILE, "r") as f:
            books = json.load(f)
        for book_id, book in books.items():
            image_path = os.path.join(IMAGE_FOLDER, book['image'])
            try:
                with open(image_path, "rb") as img_file:
                    encoded = base64.b64encode(img_file.read()).decode()
                    book['image_data'] = encoded
            except FileNotFoundError:
                print(f"⚠ Image not found: {book['image']}")
                book['image_data'] = None
        return books
    except Exception as e:
        print(f"Error loading books: {e}")
        return {}

def save_books(books):
    books_to_save = {}
    for book_id, book in books.items():
        book_copy = book.copy()
        if 'image_data' in book_copy:
            del book_copy['image_data']
        books_to_save[book_id] = book_copy
    with open(BOOKS_FILE, "w") as f:
        json.dump(books_to_save, f, indent=2)

def send_json(client_socket, data):
    try:
        encoded = json.dumps(data).encode()
        length_bytes = str(len(encoded)).ljust(10).encode()
        client_socket.sendall(length_bytes + encoded)
    except Exception as e:
        print(f"Error sending data: {e}")
        raise

def authenticate_user(username, password):
    users = load_users()
    hashed_pw = hashlib.sha256(password.encode()).hexdigest()
    if username in users and users[username]["password"] == hashed_pw:
        return True, users[username]
    return False, None

def save_order_record(username, book_id, book, quantity):
    order_entry = {
        "user": username,
        "book_id": book_id,
        "title": book.get("title"),
        "author": book.get("author"),
        "quantity_ordered": quantity,
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S")
    }

    if os.path.exists(ORDERS_FILE):
        with open(ORDERS_FILE, "r") as f:
            orders = json.load(f)
    else:
        orders = []

    orders.append(order_entry)

    with open(ORDERS_FILE, "w") as f:
        json.dump(orders, f, indent=2)


def fetch_user_orders(username):
    if os.path.exists(ORDERS_FILE):
        with open(ORDERS_FILE, "r") as f:
            orders = json.load(f)
        user_orders = [order for order in orders if order["user"] == username]
        return user_orders
    return []

def handle_client(client_socket, client_address):
    print(f"\n✅ Connected: {client_address}")
    session_id = None
    authenticated = False
    username = None

    try:
        while not authenticated:
            try:
                initial_data = client_socket.recv(1024).decode().strip()
                print(f"Initial request received: {initial_data}")

                if initial_data.startswith("AUTH:"):
                    parts = initial_data.split(":", 2)
                    if len(parts) == 3:
                        username = parts[1]
                        password = parts[2]
                        success, user_data = authenticate_user(username, password)

                        if success:
                            authenticated = True
                            session_id = base64.b64encode(os.urandom(16)).decode()
                            active_sessions[session_id] = username
                            send_json(client_socket, {
                                "status": "auth_success",
                                "session_id": session_id,
                                "username": username,
                                "role": user_data["role"]
                            })
                            print(f"👤 User authenticated: {username}")
                        else:
                            send_json(client_socket, {
                                "status": "auth_failed",
                                "message": "Invalid username or password"
                            })
                            print(f"❌ Failed login attempt for: {username}")

                elif initial_data.startswith("REGISTER:"):
                    parts = initial_data.split(":", 2)
                    if len(parts) == 3:
                        new_username = parts[1]
                        new_password = parts[2]
                        users = load_users()

                        if new_username in users:
                            send_json(client_socket, {
                                "status": "register_failed",
                                "message": "Username already exists"
                            })
                        else:
                            users[new_username] = {
                                "password": hashlib.sha256(new_password.encode()).hexdigest(),
                                "role": "customer"
                            }
                            save_users(users)
                            send_json(client_socket, {
                                "status": "register_success",
                                "message": "Account created. Please login."
                            })
                            print(f"👥 New user registered: {new_username}")
                else:
                    send_json(client_socket, {
                        "status": "error",
                        "message": "Authentication required"
                    })
            except Exception as e:
                print(f"💥 Error during authentication: {e}")
                break

        if authenticated:
            books = load_books_with_images()
            send_json(client_socket, books)
            print(f"📤 Sent full book list to {username}")

            while True:
                try:
                    request = client_socket.recv(1024).decode().strip()
                    if not request:
                        break

                    books = load_books_with_images()
                    print(f"\n📥 Received from {username}: {request}")

                    if request == "LOGOUT":
                        if session_id in active_sessions:
                            del active_sessions[session_id]
                        send_json(client_socket, {"status": "logout_success"})
                        print(f"🚪 User logged out: {username}")
                        break

                    elif request == "FETCH":
                        send_json(client_socket, books)
                        print("🔁 Sent updated book list.")

                    elif request.startswith("GENRE:"):
                        genre = request.split(":", 1)[1].strip().lower()
                        filtered = {k: v for k, v in books.items() if v['genre'].lower() == genre}
                        send_json(client_socket, filtered)
                        print(f"🔎 Sent {len(filtered)} book(s) for genre '{genre}'")

                    elif request == "TOP":
                        top_books = sorted(books.items(), key=lambda x: -x[1]['quantity'])[:3]
                        send_json(client_socket, dict(top_books))
                        print(f"🌟 Sent top 3 books.")

                    elif request.startswith("SEARCH:"):
                        term = request.split(":", 1)[1].lower()
                        matched = {k: v for k, v in books.items() if term in v['title'].lower() or term in v['author'].lower()}
                        if matched:
                            send_json(client_socket, {"status": "found", "books": matched})
                            print(f"🔍 Search matched {len(matched)} book(s).")
                        else:
                            send_json(client_socket, {"status": "not_found", "message": "No matching books."})
                            print("❌ Search returned no matches.")

                    elif request.startswith("CART_ADD:"):
                        book_id = request.split(":", 1)[1]
                        if book_id in books:
                            cart_log.append(book_id)
                            send_json(client_socket, {"status": "added", "book": books[book_id]})
                            print(f"🛒 Book added to cart: {books[book_id]['title']}")

                    elif request.startswith("CART_REMOVE:"):
                        book_id = request.split(":", 1)[1]
                        if book_id in cart_log:
                            cart_log.remove(book_id)
                            send_json(client_socket, {"status": "removed", "book_id": book_id})
                            print(f"🛒 Book removed from cart: {book_id}")
                        else:
                            send_json(client_socket, {"status": "error", "message": "Book not in cart."})

                    elif request.startswith("ORDER:"):
                        book_id = request.split(":", 1)[1]
                        if book_id in books:
                            book = books[book_id]
                            if book['quantity'] > 0:
                                book['quantity'] -= 1
                                save_books(books)
                                save_order_record(username, book_id, book, quantity=1)  # 👈 Updated here
                                send_json(client_socket, {"status": "success", "book": book})
                                print(f"✅ Order placed: {book['title']} | Quantity: 1 | Remaining: {book['quantity']}")
                            else:
                                send_json(client_socket, {"status": "error", "message": "Out of stock."})
                                print(f"❌ Out of stock: {book['title']}")
                        else:
                            send_json(client_socket, {"status": "error", "message": "Invalid book ID."})
                            print("⚠ Invalid book ID during order.")


                    elif request == "ORDERS":
                        user_orders = fetch_user_orders(username)
                        send_json(client_socket, {"status": "orders", "orders": user_orders})
                        print(f"📦 Sent order history for {username}")

                    else:
                        send_json(client_socket, {"status": "error", "message": "Invalid request."})
                        print(f"⚠ Unknown request: {request}")
                except Exception as e:
                    print(f"Error handling request: {e}")
                    break

    except Exception as e:
        print(f"💥 Error with client {client_address}: {e}")
    finally:
        if session_id in active_sessions:
            del active_sessions[session_id]
        client_socket.close()
        print(f"🔌 Disconnected: {client_address}")

context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)

try:
    context.load_cert_chain(certfile="cert.pem", keyfile="key.pem")
except ssl.SSLError as e:
    print(f"[❌ SSL ERROR] Failed to load certificate: {e}")
    print("Creating self-signed certificate...")
    from subprocess import run
    run(["openssl", "req", "-x509", "-newkey", "rsa:4096", "-keyout", "key.pem",
         "-out", "cert.pem", "-days", "365", "-nodes", "-subj", "/CN=localhost"])
    context.load_cert_chain(certfile="cert.pem", keyfile="key.pem")
except FileNotFoundError as e:
    print(f"[❌ FILE ERROR] Certificate file missing: {e}")
    print("Creating self-signed certificate...")
    from subprocess import run
    run(["openssl", "req", "-x509", "-newkey", "rsa:4096", "-keyout", "key.pem",
         "-out", "cert.pem", "-days", "365", "-nodes", "-subj", "/CN=localhost"])
    context.load_cert_chain(certfile="cert.pem", keyfile="key.pem")

ensure_users_file()

bind_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
bind_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
bind_socket.bind((HOST, PORT))
bind_socket.listen(5)
print(f"🚀 SSL Server running at {HOST}:{PORT}...")

while True:
    try:
        client_socket, client_address = bind_socket.accept()
        ssl_socket = context.wrap_socket(client_socket, server_side=True)
        threading.Thread(target=handle_client, args=(ssl_socket, client_address)).start()
    except ssl.SSLError as e:
        print(f"[❌ SSL ERROR] Failed SSL handshake with client: {e}")
    except Exception as e:
        print(f"[💥 ERROR] Unexpected error: {e}")
