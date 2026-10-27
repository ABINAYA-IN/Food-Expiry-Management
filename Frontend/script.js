const API_URL = "http://localhost:8080/api/products";

const foodForm = document.getElementById("foodForm");
const foodList = document.getElementById("foodList");
const message = document.getElementById("message");

// Calculate expiry status
function getExpiryStatus(expiryDate) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const expiry = new Date(expiryDate + "T00:00:00");
    const difference = Math.round(
        (expiry - today) / (1000 * 60 * 60 * 24)
    );

    if (difference < 0) {
        return { text: "Expired", className: "status-expired" };
    } else if (difference <= 2) {
        return { text: "Expiring Soon", className: "status-expiring" };
    }

    return { text: "Fresh", className: "status-fresh" };
}

// Display products in the table
function displayProduct(product) {
    const row = document.createElement("tr");

    const values = [
        product.name,
        product.expiryDate,
        Number(product.price).toFixed(2)
    ];

    values.forEach(function (value) {
        const cell = document.createElement("td");
        cell.textContent = value;
        row.appendChild(cell);
    });

    const status = getExpiryStatus(product.expiryDate);

    const statusCell = document.createElement("td");
    statusCell.textContent = status.text;
    statusCell.className = status.className;
    row.appendChild(statusCell);

    const actionCell = document.createElement("td");
    const deleteButton = document.createElement("button");

    deleteButton.textContent = "Delete";
    deleteButton.className = "delete-btn";

    deleteButton.addEventListener("click", async function () {
        await deleteProduct(product.id);
    });

    actionCell.appendChild(deleteButton);
    row.appendChild(actionCell);

    foodList.appendChild(row);
}

// Load products from MySQL through Spring Boot
async function loadProducts() {
    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Could not load products.");
        }

        const products = await response.json();

        foodList.innerHTML = "";

        products.forEach(displayProduct);
    } catch (error) {
        message.textContent =
            "Unable to connect to backend. Please check the server.";
        console.error(error);
    }
}

// Add product to the database
foodForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = document.getElementById("foodName").value.trim();
    const expiryDate = document.getElementById("expiryDate").value;
    const price = Number(document.getElementById("price").value);

    if (!name || !expiryDate || !Number.isFinite(price) || price <= 0) {
        message.textContent = "Please enter valid product details.";
        return;
    }

    const product = {
        name: name,
        expiryDate: expiryDate,
        price: price
    };

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(product)
        });

        if (!response.ok) {
            throw new Error("Could not save product.");
        }

        await loadProducts();

        message.textContent = "Product saved successfully!";
        foodForm.reset();

    } catch (error) {
        message.textContent = "Failed to save product. Check backend.";
        console.error(error);
    }
});

// Delete product from the database
async function deleteProduct(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error("Could not delete product.");
        }

        await loadProducts();
        message.textContent = "Product deleted successfully!";

    } catch (error) {
        message.textContent = "Failed to delete product.";
        console.error(error);
    }
}

// Load saved products when the page opens
loadProducts();
