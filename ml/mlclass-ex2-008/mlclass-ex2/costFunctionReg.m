function [J, grad] = costFunctionReg(theta, X, y, lambda)
%COSTFUNCTIONREG Compute cost and gradient for logistic regression with regularization
%   J = COSTFUNCTIONREG(theta, X, y, lambda) computes the cost of using
%   theta as the parameter for regularized logistic regression and the
%   gradient of the cost w.r.t. to the parameters. 

% Initialize some useful values
m = length(y); % number of training examples

% You need to return the following variables correctly 
J = 0;
grad = zeros(size(theta));

% ====================== YOUR CODE HERE ======================
% Instructions: Compute the cost of a particular choice of theta.
%               You should set J to the cost.
%               Compute the partial derivatives and set grad to the partial
%               derivatives of the cost w.r.t. each parameter in
%               theta
% Effectively, these are flags saying for each theta whether it
% should be taken into account as a regularization element.
shouldRegularizeJ = ones(1, size(theta)(1));
shouldRegularizeJ(1,1) = 0;
shouldRegularizeGradient = eye(size(theta)(1));
shouldRegularizeGradient(1,1) = 0;


function res = gradBase()
    res =  (h(X, theta) - y)' * X;
end

jRegularizations = (lambda / (2 * m)) * shouldRegularizeJ * (theta .^ 2) ;

J = (-y' * log(h(X, theta)) - ((1 - y)' * log(1 - h(X, theta)))) / m + jRegularizations;

gradientRegularizations = lambda * theta' * shouldRegularizeGradient;

grad = (1/m) * (gradBase() + gradientRegularizations);


% =============================================================

end
