const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const form = document.getElementById("form-inscritos");
const input = document.getElementById("id-programa");
const error = document.getElementById("error-id");

form.addEventListener("submit", (event) => {
  event.preventDefault();
  const id = input.value.trim();

  if (!UUID_REGEX.test(id)) {
    error.hidden = false;
    input.focus();
    return;
  }

  error.hidden = true;
  window.location.href = `./adminInscritos.html?id=${encodeURIComponent(id)}`;
});
