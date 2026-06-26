INSERT INTO usuarios (username, senha, perfil) VALUES
('admin', '$2a$12$512847192847120948120498124', 'ADMIN'),
('gerente', '$2a$12$1209381029381029381029381', 'GERENTE'),
('vendedor', '$2a$12$019283019283019283019283', 'VENDEDOR');

INSERT INTO clientes (nome, cpf, email, telefone) VALUES
('Peter Parker', '11122233344', 'peter@dailybugle.com', '11999998888'),
('Bruce Wayne', '55566677788', 'bruce@wayneenterprises.com', '11988887777');

INSERT INTO produtos (nome, descricao, preco, qtd_estoque, categoria) VALUES
('Mangá One Piece Vol. 1', 'Início da saga do East Blue', 49.90, 50, 'MANGA'),
('Livro Duna', 'Ficção Científica de Frank Herbert', 120.00, 15, 'LIVRO_FICCAO'),
('Action Figure Luffy Gear 5', 'Figure oficial colecionável', 280.00, 10, 'ACTION_FIGURE'),
('Jogo Catan', 'Jogo de tabuleiro moderno', 320.00, 8, 'JOGO_TABULEIRO');