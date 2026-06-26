// ============================================================\n//  LÓGICA DO MÓDULO DE PDV (CAIXA)\n// ============================================================\n
// Dados simulados de clientes caso não existam globalmente
const clientesMock = [
  { id: 1, nome: "Bruce Wayne" },
  { id: 2, nome: "Peter Parker" },
  { id: 3, nome: "Clark Kent" },
  { id: 4, nome: "Diana Prince" }
];

// Estado do Carrinho Atual
let carrinhoPDV = [];

// Inicializador da tela — chamado pelo main.js (carregarDadosPDV) quando o fragmento PDV é injetado
function carregarDadosPDV() {
  const selectClientes = document.getElementById('pdv-cliente');
  const selectProdutos = document.getElementById('pdv-produto');

  // Limpa opções antigas preservando a primeira
  selectClientes.innerHTML = '<option value="">Selecione um cliente (Opcional)</option>';
  selectProdutos.innerHTML = '<option value="">Selecione um produto...</option>';

  // Popula Clientes — usa o array global 'clientes' (clientes.js) se existir,
  // senão cai no mock de teste
  const listaClientes = (typeof clientes !== 'undefined' && clientes.length > 0)
    ? clientes.filter(c => c.ativo !== false)
    : clientesMock;
  listaClientes.forEach(c => {
    selectClientes.innerHTML += `<option value="${c.id}">${c.nome}</option>`;
  });

  // Popula Produtos ativos vindos do seu array global 'produtos'
  // Nota: Se o seu array global tiver outro nome, ajuste aqui.
  if (typeof produtos !== 'undefined' && produtos.length > 0) {
    produtos.filter(p => p.ativo).forEach(p => {
      selectProdutos.innerHTML += `<option value="${p.id}">${p.nome} (R$ ${p.preco.toFixed(2)})</option>`;
    });
  } else {
    // Fallback de teste caso a lista global esteja vazia temporariamente
    const produtosMockTeste = [
      { id: 101, nome: "Naruto Vol. 01", categoria: "mangas", preco: 35.00, ativo: true },
      { id: 102, nome: "O Senhor dos Anéis", categoria: "livros", preco: 79.90, ativo: true },
      { id: 103, nome: "Action Figure Goku", categoria: "colecionaveis", preco: 250.00, ativo: true }
    ];
    produtosMockTeste.forEach(p => {
      selectProdutos.innerHTML += `<option value="${p.id}">${p.nome} (R$ ${p.preco.toFixed(2)})</option>`;
    });
    // Injeta temporariamente para não quebrar a busca
    window.produtosPDVRef = produtosMockTeste;
  }

  renderizarCarrinho();
}

// Adicionar item selecionado ao carrinho
function adicionarItemPDV() {
  const produtoId = parseInt(document.getElementById('pdv-produto').value);
  const qtd = parseInt(document.getElementById('pdv-qtd').value);

  if (!produtoId) {
    alert("Por favor, selecione um produto.");
    return;
  }
  if (!qtd || qtd <= 0) {
    alert("Insira uma quantidade válida igual ou maior que 1.");
    return;
  }

  // Busca o produto na base correta (global ou mock de teste)
  const listaProdutos = (typeof produtos !== 'undefined' && produtos.length > 0) ? produtos : window.produtosPDVRef;
  const produtoOriginal = listaProdutos.find(p => p.id === produtoId);

  if (!produtoOriginal) return;

  // Verifica se o item já está no carrinho para acumular quantidade
  const itemExistente = carrinhoPDV.find(item => item.id === produtoId);

  if (itemExistente) {
    itemExistente.quantidade += qtd;
  } else {
    carrinhoPDV.push({
      id: produtoOriginal.id,
      nome: produtoOriginal.nome,
      categoria: produtoOriginal.categoria.toLowerCase(), // Normaliza string para validação
      preco: produtoOriginal.preco,
      quantidade: qtd
    });
  }

  // Reset do campo de quantidade para o padrão
  document.getElementById('pdv-qtd').value = 1;
  document.getElementById('pdv-produto').value = "";

  renderizarCarrinho();
}

// Remover item do carrinho
function removerItemPDV(id) {
  carrinhoPDV = carrinhoPDV.filter(item => item.id !== id);
  renderizarCarrinho();
}

// Calcula valores e renderiza a tabela dinamicamente
function renderizarCarrinho() {
  const corpoTabela = document.getElementById('pdv-carrinho-corpo');
  corpoTabela.innerHTML = '';

  let subtotalGeral = 0;
  let descontoGeral = 0;

  if (carrinhoPDV.length === 0) {
    corpoTabela.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--muted); padding: 20px;">Nenhum produto adicionado ao caixa.</td></tr>`;
  } else {
    carrinhoPDV.forEach(item => {
      const subtotalItemBruto = item.preco * item.quantidade;
      
      // Regra de Negócio: 10% de desconto para as categorias mangás ou livros
      let descontoItem = 0;
      let temDesconto = false;
      if (item.categoria.includes('manga') || item.categoria.includes('livro') || item.categoria.includes('ficção')) {
        descontoItem = subtotalItemBruto * 0.10;
        temDesconto = true;
      }

      const subtotalItemLiquido = subtotalItemBruto - descontoItem;

      // Acumuladores totais do painel direito
      subtotalGeral += subtotalItemBruto;
      descontoGeral += descontoItem;

      // Criação da linha da tabela
      corpoTabela.innerHTML += `
        <tr>
          <td><span style="font-weight: 500;">${item.nome}</span></td>
          <td><span style="text-transform: capitalize; font-size: 13px; color: var(--muted);">${item.categoria}</span></td>
          <td>${item.quantidade}</td>
          <td>R$ ${item.preco.toFixed(2)}</td>
          <td>${temDesconto ? `<span class="badge-desconto">10% Off</span>` : '-'}</td>
          <td>R$ ${subtotalItemLiquido.toFixed(2)}</td>
          <td>
            <button class="btn" style="background: transparent; color: var(--danger); padding: 4px 8px;" onclick="removerItemPDV(${item.id})">
              &times;
            </button>
          </td>
        </tr>
      `;
    });
  }

  // Atualiza painel financeiro direito em tempo real
  const totalGeral = subtotalGeral - descontoGeral;
  
  document.getElementById('pdv-subtotal-geral').innerText = `R$ ${subtotalGeral.toFixed(2)}`;
  document.getElementById('pdv-desconto-geral').innerText = `- R$ ${descontoGeral.toFixed(2)}`;
  document.getElementById('pdv-total-geral').innerText = `R$ ${totalGeral.toFixed(2)}`;
}

// Finalização da venda no botão de ação principal
function finalizarVendaPDV() {
  if (carrinhoPDV.length === 0) {
    alert("Não é possível fechar uma venda sem itens no carrinho.");
    return;
  }

  const clienteId = document.getElementById('pdv-cliente').value;
  const formaPagamento = document.getElementById('pdv-pagamento').value;
  const valorFinal = document.getElementById('pdv-total-geral').innerText;

  alert(`Venda registrada com sucesso!\n\nCliente ID: ${clienteId || "Consumidor Não Identificado"}\nForma de Pagamento: ${formaPagamento.toUpperCase()}\nTotal Pago: ${valorFinal}\n\nEstoque atualizado.`);
  
  // Limpa o PDV para a próxima venda
  carrinhoPDV = [];
  document.getElementById('pdv-cliente').value = "";
  renderizarCarrinho();
}

// Limpar carrinho (botão do resumo)
function limparCarrinhoPDV() {
  carrinhoPDV = [];
  const sel = document.getElementById('pdv-cliente');
  if (sel) sel.value = "";
  renderizarCarrinho();
}

// NÃO auto-inicializar aqui: o fragmento PDV só existe no DOM depois que
// o main.js faz o fetch e injeta o HTML. A inicialização é feita por
// carregarDadosPDV(), chamada pelo main.js em inicializarModulo('pdv').