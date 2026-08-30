const express = require('express');
const cors = require('cors');
require('ioredis');
require('dotenv').config();
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();
const PORT = process.env.REDIS-PORT
const redis = new Redis({
    host: process.env.REDIS-HOST || 'redis-app',
    port: process.env.REDIS-PORT || 6379
});

redis.on('connect', () => console.log('Conexão ao Redis feita com sucesso'));
redis.on('error', (err) => console.error('Erro de conexão com o Redis:', err));

app.use(express.json());
app.use(cors());


app.get('/health', (req, res) => {
  res.status(200).json({status: 'UP', service: 'API Gateway está estável'})
});

app.post('/reboot', (req, res) => {
  res.status(200).json({status: 'OK', message: 'API Gateway foi reiniciado'})
});



app.listen(PORT, () => console.log('API Gateway rodando na porta ${PORT}'));