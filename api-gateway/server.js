const express = require('express');
const app = express();

app.use(express.json());

app.get('/health', (req, res) => {
  res.json({ status: 'UP' });
});

app.post('/reboot', (req, res) => {
  res.json({ message: 'Reiniciando o serviço...' });
});

app.listen(3000, () => console.log('api-gateway rodando na porta 3000'));