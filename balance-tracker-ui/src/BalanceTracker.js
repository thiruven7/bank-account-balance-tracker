import React, { useState, useEffect } from 'react';
import axios from 'axios';

function BalanceTracker() {
    const [balance, setBalance] = useState(null);
    const [error, setError] = useState(null);
    useEffect(() => {
        const fetchBalance = () => {
            axios.get('/api/bankaccount/v1/balance').then((response) => {
                setBalance(response.data.balance);
                setError(null);
            })
                .catch((error) => {
                    setError(error.message);
                })
        };
        fetchBalance(); // initial fetch
        const interval = setInterval(fetchBalance, 3000); // refresh every 3 seconds
        return () => clearInterval(interval);
    }, []);

    return (
        <div style={{ margin: '2rem' }}>
            <h2>Bank Account Summary</h2>
            <p><strong>Account ID:</strong>1234567890</p>
            {error ? (<p style={{ color: 'red' }}>Error: {error}</p>)
                : (<p><strong>Balance:</strong> £{balance}</p>)}
        </div>
    )
}

export default BalanceTracker;