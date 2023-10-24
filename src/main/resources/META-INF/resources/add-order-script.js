
document.addEventListener('DOMContentLoaded', function() {
    const orderForm = document.getElementById('orderForm');
    orderForm.addEventListener('submit', function(event) {
        submitOrder(event);
    });
});
function fillWithRandomData() {
    fetch('https://random-data-api.com/api/coffee/random_coffee')
        .then(response => response.json())
        .then(data => {
            const jsonInput = document.getElementById('jsonInput');
            jsonInput.value = JSON.stringify(data, null, 2);
        })
        .catch(error => console.error('Error:', error));
}
function submitOrder(event) {
    event.preventDefault();
    const jsonInput = document.getElementById('jsonInput').value;
    const jsonData = JSON.parse(jsonInput);

    fetch('/order-api/order', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(jsonData)
    })
        .then(response => response.json())
        .then(data => {
            console.log('New Order:', data);
            const outputDiv = document.getElementById('output');
            outputDiv.innerText = JSON.stringify(data, null, 2);
        })
        .catch(error => console.error('Error:', error));
}