import { expect, test, vi } from "vite-plus/test";
import { EventEmitter } from "node:events";
import { createLoginThrottle } from "../src/modules/perfiles/http/auth.throttle.js";

function attempt(throttle, email, status = 401) {
  const res = Object.assign(new EventEmitter(), {
    statusCode: status,
    set: vi.fn(),
    status(code) {
      this.statusCode = code;
      return this;
    },
    json: vi.fn(),
  });
  const next = vi.fn(() => res.emit("finish"));
  throttle({ body: { email } }, res, next);
  return { res, next };
}
test("25 fallos bloquean la cuenta normalizada; otra cuenta puede continuar", () => {
  const throttle = createLoginThrottle();
  for (let i = 0; i < 25; i++) expect(attempt(throttle, "ANA@gmail.com").next).toHaveBeenCalled();
  const blocked = attempt(throttle, " ana@GMAIL.com ");
  expect(blocked.next).not.toHaveBeenCalled();
  expect(blocked.res.statusCode).toBe(429);
  expect(blocked.res.set).toHaveBeenCalledWith("Retry-After", "600");
  expect(attempt(throttle, "otra@gmail.com").next).toHaveBeenCalled();
});
test("expiran la ventana de un minuto y el bloqueo de diez minutos", () => {
  let now = 0;
  const throttle = createLoginThrottle({ clock: () => now, maxFailures: 2 });
  attempt(throttle, "ana");
  now = 60001;
  expect(attempt(throttle, "ana").next).toHaveBeenCalled();
  attempt(throttle, "ana");
  expect(attempt(throttle, "ana").res.statusCode).toBe(429);
  now += 600001;
  expect(attempt(throttle, "ana").next).toHaveBeenCalled();
});
test("un inicio exitoso limpia los intentos anteriores", () => {
  const throttle = createLoginThrottle({ maxFailures: 2 });
  attempt(throttle, "ana");
  attempt(throttle, "ana", 200);
  expect(attempt(throttle, "ana").next).toHaveBeenCalled();
  expect(attempt(throttle, "ana", 200).next).toHaveBeenCalled();
});
