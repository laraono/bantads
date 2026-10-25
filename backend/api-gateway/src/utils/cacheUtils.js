const redis = require("../config/redis");

const CACHE_TTL = 60 * 5;

function cacheObject(type, object) {
  redis
    .set(`cache:${type}:${object.cpf}`, JSON.stringify(object), "EX", CACHE_TTL)
    .catch(() => {});
}

async function getByCache(type, cpf) {
  try {
    const cached = await redis.get(`cache:${type}:${cpf}`);
    return cached ? JSON.parse(cached) : null;
  } catch {
    return null;
  }
}

function removeCache(type, cpf) {
  redis.del(`cache:${type}:${cpf}`).catch(() => {});
}

module.exports = { cacheObject, getByCache, removeCache };
