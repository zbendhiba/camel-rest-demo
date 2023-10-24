function fetchOrders() {
    fetch('/order-api/order')
        .then(response => response.json())
        .then(data => {
            const table = document.getElementById('outputTable');
            clearTable(table);
            if (Array.isArray(data) && data.length > 0) {
                const tableHeader = Object.keys(data[0]);
                generateTable(table, data, tableHeader);
            } else {
                table.innerHTML = "<tr><td>No Orders found</td></tr>";
            }
        })
        .catch(error => console.error('Error:', error));
}

function clearTable(table) {
    table.innerHTML = "";
}

function generateTable(table, data, tableHeader) {
    let tbody = table.createTBody();
    let headerRow = tbody.insertRow();
    for (let key of tableHeader) {
        let headerCell = headerRow.insertCell();
        let text = document.createTextNode(key);
        headerCell.appendChild(text);
        headerCell.style.fontWeight = "bold"; // Add bold to the header cells
    }

    for (let element of data) {
        let dataRow = tbody.insertRow();
        for (let key in element) {
            let dataCell = dataRow.insertCell();
            let text = document.createTextNode(element[key]);
            dataCell.appendChild(text);
        }
    }
}

function getOrderById(event) {
    event.preventDefault();
    const orderId = document.getElementById('orderId').value;
    fetch(`/order-api/order/${orderId}`)
        .then(response => response.json())
        .then(data => {
            const table = document.getElementById('outputTable');
            clearTable(table);
            if (Array.isArray(data) && data.length > 0) {
                const tableHeader = Object.keys(data[0]);
                generateTable(table, data, tableHeader);
            } else {
                table.innerHTML = "<tr><td>No Orders found</td></tr>";
            }
        })
        .catch(error => console.error('Error:', error));
}
