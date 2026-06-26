// ============================================================
//  LÓGICA DO MÓDULO DE PDV (CAIXA)
// ============================================================

// ===== DADOS E ESTADO =====
const clientesMock = [
  { id: 1, nome: "Bruce Wayne" },
  { id: 2, nome: "Peter Parker" },
  { id: 3, nome: "Clark Kent" },
  { id: 4, nome: "Diana Prince" }
];

let carrinhoPDV = [];
let produtosPDVRef = [];
let vendasRegistradas = [];
let vendaAtualId = 1000;

// ===== INICIALIZAÇÃO =====
function carregarDadosPDV() {
  const selectClientes = document.getElementById('pdv-cliente');
  const selectProdutos = document.getElementById('pdv-produto');

  if (!selectClientes || !selectProdutos) {
    console.error('Elementos do PDV não encontrados');
    return;
  }

  selectClientes.innerHTML = '<option value="">Selecione um cliente (Opcional)</option>';
  selectProdutos.innerHTML = '<option value="">Selecione um produto...</option>';

  // Popula Clientes
  const listaClientes = (typeof clientes !== 'undefined' && clientes.length > 0)
    ? clientes.filter(c => c.ativo !== false)
    : clientesMock;
  
  listaClientes.forEach(c => {
    selectClientes.innerHTML += `<option value="${c.id}">${c.nome}</option>`;
  });

  // Popula Produtos
  if (typeof produtos !== 'undefined' && produtos.length > 0) {
    produtosPDVRef = produtos.filter(p => p.ativo !== false);
  } else {
    produtosPDVRef = [
      { id: 101, nome: "Naruto Vol. 01", categoria: "mangas", preco: 35.00, estoque: 5, ativo: true },
      { id: 102, nome: "O Senhor dos Anéis", categoria: "livros", preco: 79.90, estoque: 2, ativo: true },
      { id: 103, nome: "Action Figure Goku", categoria: "colecionaveis", preco: 250.00, estoque: 1, ativo: true }
    ];
  }

  produtosPDVRef.forEach(p => {
    selectProdutos.innerHTML += `<option value="${p.id}">${p.nome} (R$ ${p.preco.toFixed(2)})</option>`;
  });

  renderizarCarrinho();
  renderizarListaVendas();
}

// ===== FUNÇÕES DO CARRINHO =====
function adicionarItemPDV() {
  const produtoSelect = document.getElementById('pdv-produto');
  const qtdInput = document.getElementById('pdv-qtd');
  
  const produtoId = parseInt(produtoSelect.value);
  const qtd = parseInt(qtdInput.value);

  if (!produtoId) {
    alert("Por favor, selecione um produto.");
    return;
  }
  
  if (!qtd || qtd <= 0) {
    alert("Insira uma quantidade válida igual ou maior que 1.");
    return;
  }

  const produtoOriginal = produtosPDVRef.find(p => p.id === produtoId);
  if (!produtoOriginal) {
    alert("Produto não encontrado.");
    return;
  }

  if (produtoOriginal.estoque !== undefined && qtd > produtoOriginal.estoque) {
    alert(`Estoque insuficiente. Disponível: ${produtoOriginal.estoque}`);
    return;
  }

  const itemExistente = carrinhoPDV.find(item => item.id === produtoId);

  if (itemExistente) {
    const novaQuantidade = itemExistente.quantidade + qtd;
    if (produtoOriginal.estoque !== undefined && novaQuantidade > produtoOriginal.estoque) {
      alert(`Estoque insuficiente. Disponível: ${produtoOriginal.estoque}`);
      return;
    }
    itemExistente.quantidade = novaQuantidade;
  } else {
    carrinhoPDV.push({
      id: produtoOriginal.id,
      nome: produtoOriginal.nome,
      categoria: produtoOriginal.categoria.toLowerCase(),
      preco: produtoOriginal.preco,
      quantidade: qtd,
      estoque: produtoOriginal.estoque
    });
  }

  qtdInput.value = 1;
  produtoSelect.value = "";
  renderizarCarrinho();
}

function removerItemPDV(id) {
  carrinhoPDV = carrinhoPDV.filter(item => item.id !== id);
  renderizarCarrinho();
}

function limparCarrinhoPDV() {
  if (carrinhoPDV.length === 0) {
    alert("O carrinho já está vazio.");
    return;
  }
  
  if (confirm("Deseja realmente limpar todo o carrinho?")) {
    carrinhoPDV = [];
    document.getElementById('pdv-cliente').value = "";
    renderizarCarrinho();
  }
}

// ===== FINALIZAR VENDA (com integração API) =====
async function finalizarVendaPDV() {
  if (carrinhoPDV.length === 0) {
    alert("Não é possível fechar uma venda sem itens no carrinho.");
    return;
  }

  // Verifica estoque
  const estoqueInvalido = carrinhoPDV.some(item => {
    const produto = produtosPDVRef.find(p => p.id === item.id);
    return produto && produto.estoque !== undefined && item.quantidade > produto.estoque;
  });

  if (estoqueInvalido) {
    alert("Existem produtos com estoque insuficiente. Verifique e tente novamente.");
    return;
  }

  const clienteId = document.getElementById('pdv-cliente').value;
  const formaPagamento = document.getElementById('pdv-pagamento').value;
  const valorFinal = document.getElementById('pdv-total-geral').innerText;

  // Busca nome do cliente
  let nomeCliente = "Consumidor Não Identificado";
  if (clienteId) {
    const listaClientes = (typeof clientes !== 'undefined' && clientes.length > 0)
      ? clientes
      : clientesMock;
    const cliente = listaClientes.find(c => c.id === parseInt(clienteId));
    if (cliente) nomeCliente = cliente.nome;
  }

  // Prepara dados para API
  const vendaData = {
    clienteId: clienteId || null,
    clienteNome: nomeCliente,
    formaPagamento: formaPagamento,
    itens: carrinhoPDV.map(item => ({
      produtoId: item.id,
      nome: item.nome,
      quantidade: item.quantidade,
      precoUnitario: item.preco
    })),
    total: parseFloat(valorFinal.replace('R$ ', '').replace(',', '.'))
  };

  try {
    // ===== INTEGRAÇÃO COM API =====
    console.log('Enviando venda para API:', vendaData);
    
    // Simula chamada à API (substituir pela chamada real)
    // const response = await fetch('http://localhost:8080/vendas', {
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json',
    //     'Authorization': `Bearer ${localStorage.getItem('token')}`
    //   },
    //   body: JSON.stringify(vendaData)
    // });
    
    // if (!response.ok) {
    //   throw new Error(`Erro ao finalizar venda: ${response.status}`);
    // }
    
    // const resultado = await response.json();
    
    // Simulação de resposta da API
    const resultado = {
      id: ++vendaAtualId,
      status: 'ABERTA',
      data: new Date().toISOString(),
      itens: vendaData.itens.map(item => ({
        ...item,
        estoqueAtual: produtosPDVRef.find(p => p.id === item.produtoId).estoque - item.quantidade
      }))
    };

    // Atualiza estoque local
    resultado.itens.forEach(item => {
      const produto = produtosPDVRef.find(p => p.id === item.produtoId);
      if (produto) {
        produto.estoque = item.estoqueAtual;
      }
    });

    // Registra venda localmente
    const venda = {
      id: resultado.id,
      ...vendaData,
      status: 'ABERTA',
      data: resultado.data
    };
    vendasRegistradas.unshift(venda);

    // Mensagem de sucesso
    alert(`✅ Venda #${venda.id} registrada com sucesso!\nCliente: ${nomeCliente}\nTotal: ${valorFinal}`);

    // Limpa carrinho
    carrinhoPDV = [];
    document.getElementById('pdv-cliente').value = "";
    renderizarCarrinho();
    renderizarListaVendas();

  } catch (error) {
    console.error('Erro ao finalizar venda:', error);
    alert(`❌ Erro ao finalizar venda: ${error.message}`);
  }
}

// ===== LISTAGEM DE VENDAS =====
function renderizarListaVendas() {
  const container = document.getElementById('lista-vendas-container');
  if (!container) return;

  if (vendasRegistradas.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 30px; color: var(--muted);">
        <i class="bi bi-receipt" style="font-size: 32px; display: block; margin-bottom: 8px;"></i>
        Nenhuma venda registrada ainda.
      </div>
    `;
    return;
  }

  let html = `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Cliente</th>
            <th>Data</th>
            <th>Total</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
  `;

  vendasRegistradas.forEach(venda => {
    const statusClass = venda.status === 'ABERTA' ? 'status-aberta' : 'status-cancelada';
    const statusLabel = venda.status === 'ABERTA' ? '✅ Aberta' : '❌ Cancelada';
    
    html += `
      <tr>
        <td><strong>#${venda.id}</strong></td>
        <td>${venda.clienteNome}</td>
        <td>${new Date(venda.data).toLocaleDateString('pt-BR')}</td>
        <td>R$ ${venda.total.toFixed(2)}</td>
        <td><span class="${statusClass}">${statusLabel}</span></td>
        <td>
          ${venda.status === 'ABERTA' ? `
            <button class="btn btn-danger btn-sm" onclick="cancelarVenda(${venda.id})">
              <i class="bi bi-x-circle"></i> Cancelar
            </button>
          ` : `
            <span style="color: var(--muted); font-size: 12px;">Cancelada</span>
          `}
        </td>
      </tr>
    `;
  });

  html += `
        </tbody>
      </table>
    </div>
  `;

  container.innerHTML = html;
}

// ===== CANCELAR VENDA =====
function cancelarVenda(id) {
  const venda = vendasRegistradas.find(v => v.id === id);
  
  if (!venda) {
    alert("Venda não encontrada.");
    return;
  }
  
  // Verifica se o status é ABERTA
  if (venda.status !== 'ABERTA') {
    alert("Apenas vendas com status ABERTA podem ser canceladas.");
    return;
  }
  
  if (confirm(`Deseja realmente cancelar a venda #${venda.id}?\nCliente: ${venda.clienteNome}\nTotal: R$ ${venda.total.toFixed(2)}`)) {
    // Reverte o estoque
    venda.itens.forEach(item => {
      const produto = produtosPDVRef.find(p => p.id === item.produtoId);
      if (produto) {
        produto.estoque += item.quantidade;
      }
    });
    
    // Atualiza status
    venda.status = 'CANCELADA';
    
    // Atualiza listagem
    renderizarListaVendas();
    renderizarCarrinho();
    alert(`✅ Venda #${venda.id} cancelada com sucesso!\nEstoque revertido.`);
  }
}

// ===== RENDERIZAR CARRINHO =====
function renderizarCarrinho() {
  const corpoTabela = document.getElementById('pdv-carrinho-corpo');
  if (!corpoTabela) return;

  corpoTabela.innerHTML = '';

  let subtotalGeral = 0;
  let descontoGeral = 0;

  if (carrinhoPDV.length === 0) {
    corpoTabela.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; color: var(--muted); padding: 20px;">
          <i class="bi bi-cart" style="font-size: 24px; display: block; margin-bottom: 8px;"></i>
          Nenhum produto adicionado ao caixa.
        </td>
      </tr>
    `;
  } else {
    carrinhoPDV.forEach(item => {
      const subtotalItemBruto = item.preco * item.quantidade;

      let descontoItem = 0;
      let temDesconto = false;
      const categoriaLower = item.categoria.toLowerCase();
      
      if (categoriaLower.includes('manga') || 
          categoriaLower.includes('livro') || 
          categoriaLower.includes('ficção')) {
        descontoItem = subtotalItemBruto * 0.10;
        temDesconto = true;
      }

      const subtotalItemLiquido = subtotalItemBruto - descontoItem;

      subtotalGeral += subtotalItemBruto;
      descontoGeral += descontoItem;

      corpoTabela.innerHTML += `
        <tr>
          <td><span style="font-weight: 500;">${item.nome}</span></td>
          <td><span style="text-transform: capitalize; font-size: 13px; color: var(--muted);">${item.categoria}</span></td>
          <td>${item.quantidade}</td>
          <td>R$ ${item.preco.toFixed(2)}</td>
          <td>${temDesconto ? `<span class="badge-desconto" style="background: #28a745; color: white; padding: 2px 8px; border-radius: 12px; font-size: 11px;">10% Off</span>` : '-'}</td>
          <td>R$ ${subtotalItemLiquido.toFixed(2)}</td>
          <td>
            <button class="btn-remove" style="background: transparent; border: none; color: #dc3545; padding: 4px 8px; font-size: 18px; cursor: pointer;" onclick="removerItemPDV(${item.id})">
              <i class="bi bi-x-circle"></i>
            </button>
          </td>
        </tr>
      `;
    });
  }

  const totalGeral = subtotalGeral - descontoGeral;

  document.getElementById('pdv-subtotal-geral').innerText = `R$ ${subtotalGeral.toFixed(2)}`;
  document.getElementById('pdv-desconto-geral').innerText = `- R$ ${descontoGeral.toFixed(2)}`;
  document.getElementById('pdv-total-geral').innerText = `R$ ${totalGeral.toFixed(2)}`;

  // ===== TRAVA DO BOTÃO FINALIZAR =====
  const btnFinalizar = document.getElementById("btn-finalizar");
  if (btnFinalizar) {
    const estoqueInvalido = carrinhoPDV.some(item => {
      const produto = produtosPDVRef.find(p => p.id === item.id);
      return produto && produto.estoque !== undefined && item.quantidade > produto.estoque;
    });
    btnFinalizar.disabled = carrinhoPDV.length === 0 || estoqueInvalido;
    
    if (btnFinalizar.disabled) {
      btnFinalizar.style.opacity = '0.6';
      btnFinalizar.style.cursor = 'not-allowed';
    } else {
      btnFinalizar.style.opacity = '1';
      btnFinalizar.style.cursor = 'pointer';
    }
  }
}

// ===== INICIALIZAÇÃO =====
document.addEventListener('DOMContentLoaded', function() {
  if (document.getElementById('section-pdv')) {
    carregarDadosPDV();
  }
});

// Exportar para escopo global
if (typeof window !== 'undefined') {
  window.carregarDadosPDV = carregarDadosPDV;
  window.adicionarItemPDV = adicionarItemPDV;
  window.removerItemPDV = removerItemPDV;
  window.limparCarrinhoPDV = limparCarrinhoPDV;
  window.finalizarVendaPDV = finalizarVendaPDV;
  window.cancelarVenda = cancelarVenda;
  window.renderizarListaVendas = renderizarListaVendas;
}