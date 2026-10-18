import { expect, test } from "vite-plus/test";
import { add } from "./add.js";

test("adds two numbers", () => {
  expect(add(1, 2)).toBe(3);
});
