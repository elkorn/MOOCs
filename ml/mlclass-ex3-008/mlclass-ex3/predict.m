function p = predict(Theta1, Theta2, X)
%PREDICT Predict the label of an input given a trained neural network
%   p = PREDICT(Theta1, Theta2, X) outputs the predicted label of X given the
%   trained weights of a neural network (Theta1, Theta2)

% Useful values
m = size(X, 1);
num_labels = size(Theta2, 1);

% You need to return the following variables correctly 
p = zeros(size(X, 1), 1);

% ====================== YOUR CODE HERE ======================
% Instructions: Complete the following code to make predictions using
%               your learned neural network. You should set p to a 
%               vector containing labels between 1 to num_labels.
%
% Hint: The max function might come in useful. In particular, the max
%       function can also return the index of the max element, for more
%       information see 'help max'. If your examples are in rows, then, you
%       can use max(A, [], 2) to obtain the max for each row.
%

% Add ones to the X data matrix
X = [ones(m, 1) X];

function q = propagate(layers, X)
    L=size(layers, 1);
    al = X;
    for layer=1:L
        z = al * layers(layer)';
        al = [1 sigmoid(z)];
    end

    q = max(al);
end

% This fails, I need an iterable list of matrices - such that picking an item
% from that list would be of the underlying type. Or some coercion.
layers = [Theta1;Theta2];

% The trick here is to do something along the lines of one-vs-all, but taking
% into account each layer of the network.
for example=1:m
    % % z2 equals the product of a1 and Θ1
    % z2 = X(example, :) * Theta1';
    % % a2 is the result of passing z2 through g()
    % % a2 then has a column of 1st added (bias units) as the first column.
    % a2 = [1 sigmoid(z2)];
    % % z3 equals the product of a2 and Θ2
    % z3 = a2 * Theta2';
    % % a3 is the result of passing z3 through g()
    % a3 = sigmoid(z3);
    [_ p(example)] = propagate(layers, X(example, :));
end
% =========================================================================


end
