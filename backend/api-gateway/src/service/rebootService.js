const argon2 = require('argon2');
const crypto = require('crypto');
const path = require('path');
const { exec } = require('child_process');
const { promisify } = require('util');
const { MongoClient } = require('mongodb');
const { Client } = require('pg');
const fs = require('fs/promises');

const MONGO_USER = process.env.MONGO_USER || 'admin';
const MONGO_PASS = process.env.MONGO_PASS || 'password';
const POSTGRES_USER = process.env.POSTGRES_USER || 'user';
const POSTGRES_PASS = process.env.POSTGRES_PASS || 'password';

const MONGO_URI = `mongodb://${MONGO_USER}:${MONGO_PASS}@mongodb:27017`;

const POSTGRES = [
  {
    dbName: 'client_db',
    sqlFile: 'client-seeder.sql',
  },
  {
    dbName: 'request_db',
    sqlFile: 'request-seeder.sql',
  },
  {
    dbName: 'command_db',
    sqlFile: 'command-seeder.sql',
  },
  {
    dbName: 'manager_db',
    sqlFile: 'manager-seeder.sql',
  },
];


class RebootService {

    async generatePasswordHashes(pass) {
      try {
        const options = {
            type: argon2.argon2id, 
            memoryCost: 65536,     
            timeCost: 3,           
            parallelism: 1,        
            };
        
            return await argon2.hash(pass, options);
        
        } catch (err) {
            console.error("Erro ao gerar hash Argon2:", err);
            const error = new Error(err);
            error.status = 500;
            throw error;
        }
    }

    async seedAuth() {
        const client = new MongoClient(MONGO_URI);

        try {
            await client.connect();
            const db = client.db('auths_db');
            const collection = db.collection('auths');

            await collection.createIndex({ login: 1 }, { unique: true });

            await collection.deleteMany({});

            const MONGO = [
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "12912861012",
                    user_type: "CLIENTE",
                    login: "cli1@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                },
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "09506382000",
                    user_type: "CLIENTE",
                    login: "cli2@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                },
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "85733854057",
                    user_type: "CLIENTE",
                    login: "cli3@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                },
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "58872160006",
                    user_type: "CLIENTE",
                    login: "cli4@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                },
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "76179646090",
                    user_type: "CLIENTE",
                    login: "cli5@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                },
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "98574307084",
                    user_type: "GERENTE",
                    login: "ger1@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                },
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "64065268052",
                    user_type: "GERENTE",
                    login: "ger2@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                },
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "23862179060",
                    user_type: "GERENTE",
                    login: "ger3@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                },
                {
                    _id: crypto.randomUUID(),
                    user_cpf: "40501740066",
                    user_type: "GERENTE",
                    login: "ger4@bantads.com.br",
                    password: await this.generatePasswordHashes('tads'),
                    is_active: true
                }
            ]

            await collection.insertMany(MONGO);

        } catch(err) {
            console.log(err.message)
            const error = new Error(err);
            error.status = 500;
            throw error;
        } finally {
            await client.close();
        }
    }

    async seedPostgres() {
        for (const { dbName, sqlFile } of POSTGRES) {
            const filePath = path.resolve(process.cwd(), 'src/seeder', sqlFile);

            const sqlContent = await fs.readFile(filePath, 'utf8');

            const client = new Client({
                host: 'postgres-db',
                port: 5432,
                user: POSTGRES_USER,
                password: POSTGRES_PASS,
                database: dbName,
            });

            try {
                await client.connect();

                await client.query(sqlContent);
                
                console.log(`Successfully seeded ${dbName}`);
            } catch (err) {
                const error = new Error(`Seeding failed for ${dbName}: ${err.message}`);
                error.status = 500;
                throw error;
            } finally {
                await client.end();
            }
        }
    }
}

module.exports = new RebootService()