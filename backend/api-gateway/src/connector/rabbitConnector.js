const crypto = require('crypto');
const amqp = require('amqplib');
const redis = require('../config/redis');

let channel

const RABBITMQ_USER = process.env.POSTGRES_USER || 'guest'
const RABBITMQ_PASS = process.env.POSTGRES_PASS || 'guest'
const RABBITMQ_URL = process.env.AMQP_URL || `amqp://${RABBITMQ_USER}:${RABBITMQ_PASS}@rabbitmq:5672`
const SAGA_CMD = process.env.SAGA_CMD || 'saga.cmd'

class RabbitConnector {
    async connect(retries = 5, delay = 3000) {
        while (retries > 0) {
            try {
                const connection = await amqp.connect(RABBITMQ_URL)
                channel = await connection.createChannel()
                await channel.assertQueue(SAGA_CMD)
                console.log("Connectado ao RabbitMQ")
                return channel
            } catch (err) {
                retries--
                console.log(err.message)
                if (retries === 0) throw err
                await new Promise(res => setTimeout(res, delay))
            }
        }
    }

    async startSaga(payload, job) {
        if (!channel) {
            throw new Error("RabbitMQ channel is not initialized. Call connect() first.")
        }

        // jobId == sagaId (seção 5.8); a chave no Redis é "job-<sagaId>", que é
        // a mesma que o orquestrador atualiza ao final da SAGA
        const sagaId = crypto.randomUUID()
        const jobKey = "job-" + sagaId

        // "@class" é exigido pelo serializer JSON do Spring Data Redis usado no
        // orquestrador para conseguir ler esse valor de volta como um Map
        await redis.set(jobKey, JSON.stringify({
            ...job,
            jobId: sagaId,
            "@class": "java.util.HashMap"
        }))

        channel.sendToQueue(
            SAGA_CMD,
            Buffer.from(
                JSON.stringify({
                    ...payload,
                    sagaId
                })
            )
        )

        return {jobId: jobKey}
    }
}

module.exports = new RabbitConnector();