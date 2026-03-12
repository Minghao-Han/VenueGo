const jwt = require('jsonwebtoken');
const fs = require('fs');
const { randomUUID } = require('crypto');

const privateKey = fs.readFileSync('./AuthService/src/main/resources/keys/private.pem');

const tokens = [];
const now = Math.floor(Date.now() / 1000);
const expiry = now + (3600 * 24 * 7);

for (let i = 1; i <= 10000; i++) {
    const userId = randomUUID();   // 改这里
    const jti = randomUUID();

    const payload = {
        iss: "venuego-auth-service",
        iat: now,
        exp: expiry,
        sub: userId,                // 现在是 UUID
        jti: jti,
        email: `user${i}@example.com`,
        roles: ["ROLE_USER"]
    };

    const token = jwt.sign(payload, privateKey, { algorithm: 'RS256' });
    tokens.push(token);
}

fs.writeFileSync('tokens.txt', tokens.join('\n'));
console.log("成功生成 10,000 个带 UUID userId 的 JWT");