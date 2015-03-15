x = [0.256; 0.054; 0.249];
Theta1 = magic(3);
a2 = zeros (3, 1);
for i = 1:3
  for j = 1:3
    a2(i) = a2(i) + x(j) * Theta1(i, j);
  end
  a2(i) = sigmoid (a2(i));
end

a2

a2 = sigmoid(Theta1 * x)
z = sigmoid(x); a2 = Theta1 * z
z = sigmoid(x); a2 = sigmoid(Theta1 * z)
