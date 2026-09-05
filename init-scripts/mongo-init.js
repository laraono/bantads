db = db.getSiblingDB('account_mongo');

db.auths.insertMany([
  {
    _id: '8797e837-c6fa-400f-9d75-3b6c4d3e5adb',
    login: 'cli1@bantads.com.br',
    password: '$argon2id$v=19$m=15360,t=2,p=1$GIZ82+S79Qk7eo2xAq8TRz3AzoAT8ylxSr56unqmpBArfDlCwetryG4FImbv68djG/lo2zeQhbYkZwX1J6zRpw$eGisoeMmgtOZ/Lgkx6dBX4ppQHUiJPnFA1hHITyCGnM',
    user_cpf: '12912861012',
    user_type: 'CLIENTE',
    is_active: true,
    _class: 'com.bantads.entity.Auth'
  },
  {
    _id: 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    login: 'ger1@bantads.com.br',
    password: '$argon2id$v=19$m=15360,t=2,p=1$GIZ82+S79Qk7eo2xAq8TRz3AzoAT8ylxSr56unqmpBArfDlCwetryG4FImbv68djG/lo2zeQhbYkZwX1J6zRpw$eGisoeMmgtOZ/Lgkx6dBX4ppQHUiJPnFA1hHITyCGnM',
    user_cpf: '98765432100',
    user_type: 'GERENTE',
    is_active: true,
    _class: 'com.bantads.entity.Auth'
  }
]);
