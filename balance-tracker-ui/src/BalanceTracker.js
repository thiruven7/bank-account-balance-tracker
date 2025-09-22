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
        <div className="container mt-5">
            <div className="card shadow-sm">
                <div className="card-body">
                    <h2 className="card-title text-primary">Bank Account Summary</h2>
                    <p><strong>Bank Account Number:</strong>ACC123456</p>
                    {error ? (<p className="text-danger">Error: {error}</p>)
                        : (<p><strong>Balance:</strong> £{balance}</p>)}
                </div>
            </div>
        </div>
    )
}

export default BalanceTracker;