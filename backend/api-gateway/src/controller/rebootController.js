const rebootService = require('../service/rebootService');

class RebootController {
    async reboot(req, res) {
        try {
            await rebootService.seedAuth()
            await rebootService.seedPostgres()

            return res.status(200).json({ message: 'Seeder concluído' });
        } catch (err) {
            const status = err.status || 500;
            console.log(err)
            return res.status(status).json({ error: err.message || 'Falha no seeder' });
        }
    }
}

module.exports = new RebootController();