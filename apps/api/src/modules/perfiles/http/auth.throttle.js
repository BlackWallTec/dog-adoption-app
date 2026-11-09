// Bloqueo por cuenta: 25 fallos en un minuto suspenden el acceso durante 10 minutos.
export function createLoginThrottle({ clock = Date.now, maxFailures = 25 } = {}) {
  const accounts = new Map();
  return (req, res, next) => {
    const key = typeof req.body?.email === "string" ? req.body.email.trim().toLowerCase() : "";
    if (!key || key.length > 254) return next();
    const now = clock();
    for (const [email, entry] of accounts) {
      if ((entry.blockedUntil || entry.since + 60000) <= now) accounts.delete(email);
    }
    const entry = accounts.get(key);
    if (entry?.blockedUntil > now) {
      res.set("Retry-After", String(Math.ceil((entry.blockedUntil - now) / 1000)));
      return res.status(429).json({ error: "Demasiados intentos. Inténtalo en 10 minutos." });
    }
    res.once("finish", () => {
      if (res.statusCode === 200) return void accounts.delete(key);
      if (res.statusCode !== 401) return;
      const failedAt = clock();
      let current = accounts.get(key);
      if (!current || (!current.blockedUntil && failedAt - current.since >= 60000)) {
        if (accounts.size >= 10000) accounts.delete(accounts.keys().next().value);
        current = { since: failedAt, failures: 0, blockedUntil: 0 };
        accounts.set(key, current);
      }
      if (++current.failures >= maxFailures) current.blockedUntil = failedAt + 600000;
    });
    next();
  };
}
