const express = require('express');
const cors = require('cors');
const Redis = require('ioredis');
require('dotenv').config();
const authRoutes = require('./src/routes/authRoutes');

const app = express();
const PORT = process.env.PORT || 3000;
const redis = new Redis({
    host: process.env.REDIS_HOST || 'redis-app',
    port: Number(process.env.REDIS_PORT) || 6379
});

redis.on('connect', () => console.log('Conexão ao Redis feita com sucesso'));
redis.on('error', (err) => console.error('Erro de conexão com o Redis:', err));

app.use(express.json());
app.use(cors());

app.use('/api/auth', authRoutes);

app.get('/health', (req, res) => {
  res.status(200).json({status: 'UP', service: 'API Gateway está estável'})
});

app.post('/reboot', (req, res) => {
  res.status(200).json({status: 'OK', message: 'API Gateway foi reiniciado'})
});

