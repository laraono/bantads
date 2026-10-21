const redis = require("../config/redis");

const CACHE_TTL = 60 * 5;

function cacheObject(type, object) {
  redis
    .set(`cache:${type}:${object.cpf}`, JSON.stringify(object), "EX", CACHE_TTL)
    .catch(() => {});
}

function getByCache(type, cpf) {
  const cached = redis.get(`cache:${type}:${cpf}`).catch(() => {});
  return JSON.parse(cached);
}

function removeCache(type, cpf) {
  redis.del(`cache:${type}:${cpf}`).catch(() => {});
}

module.exports = { cacheObject, getByCache, removeCache };
