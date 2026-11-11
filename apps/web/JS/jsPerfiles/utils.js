export function fieldError(input, message = "") {
  input.classList.toggle("is-invalid", Boolean(message));
  input.setAttribute("aria-invalid", String(Boolean(message)));
  byId(`${input.id}-error`).textContent = message;
}

export const byId = (id) => document.getElementById(id);
