const argon2 = require("argon2");
const crypto = require("crypto");
const path = require("path");
const { MongoClient } = require("mongodb");
const { Client } = require("pg");
const fs = require("fs/promises");
const redis = require("../config/redis");

const MONGO_USER = process.env.MONGO_USER || "admin";
const MONGO_PASS = process.env.MONGO_PASS || "password";
const POSTGRES_USER = process.env.POSTGRES_USER || "user";
const POSTGRES_PASS = process.env.POSTGRES_PASS || "password";

const MONGO_URI = `mongodb://${MONGO_USER}:${MONGO_PASS}@mongodb:27017`;

const POSTGRES = [
  {
    dbName: "client_db",
    sqlFile: "client-seeder.sql",
  },
  {
    dbName: "request_db",
    sqlFile: "request-seeder.sql",
  },
  {
    dbName: "command_db",
    sqlFile: "command-seeder.sql",
  },
  {
    dbName: "manager_db",
    sqlFile: "manager-seeder.sql",
  },
];

class RebootService {
  async generatePasswordHashes(pass) {
    try {
      const options = {
        type: argon2.argon2id,
        memoryCost: 15360,
        timeCost: 2,
        parallelism: 1,
      };

      const hash = await argon2.hash(pass, options);
      return hash.replace(/m=(\d+),p=(\d+),t=(\d+)/, "m=$1,t=$3,p=$2");
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
      const db = client.db("account_mongo");
      const collection = db.collection("auths");

      await collection.createIndex({ email: 1 }, { unique: true });

      await collection.deleteMany({});

      const MONGO = [
        {
          _id: crypto.randomUUID(),
          user_cpf: "12912861012",
          user_type: "CLIENTE",
          email: "cli1@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
        {
          _id: crypto.randomUUID(),
          user_cpf: "09506382000",
          user_type: "CLIENTE",
          email: "cli2@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
        {
          _id: crypto.randomUUID(),
          user_cpf: "85733854057",
          user_type: "CLIENTE",
          email: "cli3@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
        {
          _id: crypto.randomUUID(),
          user_cpf: "58872160006",
          user_type: "CLIENTE",
          email: "cli4@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
        {
          _id: crypto.randomUUID(),
          user_cpf: "76179646090",
          user_type: "CLIENTE",
          email: "cli5@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
        {
          _id: crypto.randomUUID(),
          user_cpf: "98574307084",
          user_type: "GERENTE",
          email: "ger1@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
        {
          _id: crypto.randomUUID(),
          user_cpf: "64065268052",
          user_type: "GERENTE",
          email: "ger2@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
        {
          _id: crypto.randomUUID(),
          user_cpf: "23862179060",
          user_type: "GERENTE",
          email: "ger3@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
        {
          _id: crypto.randomUUID(),
          user_cpf: "40501740066",
          user_type: "GERENTE",
          email: "ger4@bantads.com.br",
          password: await this.generatePasswordHashes("tads"),
          is_active: true,
        },
      ];

      await collection.insertMany(MONGO);

      const clientes = MONGO.filter((m) => m.user_type === "CLIENTE").length;
      const gerentes = MONGO.filter((m) => m.user_type === "GERENTE").length;
      const contas = MONGO.filter((m) => m.user_type === "CONTA").length;

      return { clientes, gerentes, contas };
      
    } catch (err) {
      console.log(err.message);
      const error = new Error(err);
      error.status = 500;
      throw error;
    } finally {
      await client.close();
    }
  }

  async seedPostgres() {
    for (const { dbName, sqlFile } of POSTGRES) {
      const filePath = path.resolve(process.cwd(), "src/seeder", sqlFile);

      const sqlContent = await fs.readFile(filePath, "utf8");

      const client = new Client({
        host: "postgres-db",
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

  async flushRedis() {
    await redis.flushAll();
  }
}

module.exports = new RebootService();
