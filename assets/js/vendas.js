// js/vendas.js
// Tela de Vendas - carrinho, finalizacao, historico e cancelamento
// (funciona 100% com o mock do api.js, sem backend)

// ============================================================
//  ESTADO DO CARRINHO
// ============================================================

let carrinho = [];                 // [{ produtoId, produtoNome, quantidade, precoUnitario, subtotal }]
let produtosCache = [];            // produtos ativos carregados
const DESCONTO_PERCENTUAL = 0.10;  // 10%
const DESCONTO_MINIMO = 300;       // aplica desconto quando subtotal >= R$ 300 (ajuste a vontade)

// ============================================================
//  HELPERS
// ============================================================

function formatBRL(valor) {
    const n = Number(valor) || 0;
    return 'R$ ' + n.toFixed(2).replace('.', ',');
}

function formatarDataVenda(data) {
    if (!data) return '-';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

function badgeStatus(status) {
    const map = {
        FINALIZADA: 'badge-success',
        ABERTA: 'badge-warning',
        CANCELADA: 'badge-danger'
    };
    const cls = map[status] || 'badge-primary';
    return `<span class="badge ${cls}">${status}</span>`;
}

// ============================================================
//  CARREGAR DADOS (selects de cliente e produto)
// ============================================================

async function carregarDadosVenda() {
    try {
        const [clientes, produtos] = await Promise.all([
            apiClientes.listar(),
            apiProdutos.listar()
        ]);

        // --- Select de clientes (apenas ativos) ---
        const selCliente = document.getElementById('clienteVenda');
        if (selCliente) {
            const atual = selCliente.value;
            selCliente.innerHTML = '<option value="">Selecione um cliente...</option>';
            clientes.filter(c => c.ativo).forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = c.nome;
                selCliente.appendChild(opt);
            });
            selCliente.value = atual; // preserva selecao se possivel
        }

        // --- Select de produtos (ativos e com estoque) ---
        produtosCache = produtos.filter(p => p.ativo);
        const selProduto = document.getElementById('produtoVenda');
        if (selProduto) {
            selProduto.innerHTML = '<option value="">Selecione um produto...</option>';
            produtosCache.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.id;
                opt.dataset.preco = p.preco;
                opt.dataset.estoque = p.estoque;
                const semEstoque = p.estoque <= 0 ? ' (sem estoque)' : ` (estoque: ${p.estoque})`;
                opt.textContent = `${p.nome}${semEstoque}`;
                if (p.estoque <= 0) opt.disabled = true;
                selProduto.appendChild(opt);
            });
        }
    } catch (error) {
        console.error('Erro ao carregar dados da venda:', error);
        mostrarToast('Erro ao carregar clientes/produtos', 'danger');
    }
}

// ============================================================
//  CONFIGURAR EVENTOS
// ============================================================

function configurarEventosVendas() {
    const selProduto = document.getElementById('produtoVenda');
    if (selProduto) {
        selProduto.onchange = () => {
            const opt = selProduto.options[selProduto.selectedIndex];
            const preco = opt && opt.dataset ? opt.dataset.preco : '';
            const inputPreco = document.getElementById('precoUnitario');
            if (inputPreco) inputPreco.value = preco ? formatBRL(preco) : '';
        };
    }

    const btnAdd = document.getElementById('btnAdicionarItem');
    if (btnAdd) btnAdd.onclick = adicionarItem;

    const btnFinalizar = document.getElementById('btnFinalizarVenda');
    if (btnFinalizar) btnFinalizar.onclick = finalizarVenda;

    const btnCancelar = document.getElementById('btnCancelarVenda');
    if (btnCancelar) btnCancelar.onclick = cancelarVenda;
}

// ============================================================
//  CARRINHO
// ============================================================

function adicionarItem() {
    const selProduto = document.getElementById('produtoVenda');
    const inputQtd = document.getElementById('quantidadeItem');

    const produtoId = parseInt(selProduto?.value);
    const quantidade = parseInt(inputQtd?.value);

    if (!produtoId) {
        mostrarToast('Selecione um produto', 'warning');
        return;
    }
    if (!quantidade || quantidade < 1) {
        mostrarToast('Quantidade invalida', 'warning');
        return;
    }

    const produto = produtosCache.find(p => p.id === produtoId);
    if (!produto) {
        mostrarToast('Produto nao encontrado', 'danger');
        return;
    }

    // Validar estoque (considerando o que ja esta no carrinho)
    const noCarrinho = carrinho.find(i => i.produtoId === produtoId);
    const qtdJa = noCarrinho ? noCarrinho.quantidade : 0;
    if (qtdJa + quantidade > produto.estoque) {
        mostrarToast(`Estoque insuficiente (disponivel: ${produto.estoque})`, 'warning');
        return;
    }

    if (noCarrinho) {
        noCarrinho.quantidade += quantidade;
        noCarrinho.subtotal = noCarrinho.quantidade * noCarrinho.precoUnitario;
    } else {
        carrinho.push({
            produtoId: produto.id,
            produtoNome: produto.nome,
            quantidade,
            precoUnitario: produto.preco,
            subtotal: quantidade * produto.preco
        });
    }

    // Resetar campos de entrada
    if (selProduto) selProduto.value = '';
    if (inputQtd) inputQtd.value = '1';
    const inputPreco = document.getElementById('precoUnitario');
    if (inputPreco) inputPreco.value = '';

    renderizarCarrinho();
}

function removerItem(produtoId) {
    carrinho = carrinho.filter(i => i.produtoId !== produtoId);
    renderizarCarrinho();
}

function renderizarCarrinho() {
    const tbody = document.getElementById('tabelaCarrinho');
    const badgeItens = document.getElementById('totalItensCarrinho');

    if (!tbody) return;

    if (carrinho.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="empty">
                    <p>Carrinho vazio. Adicione produtos.</p>
                </td>
            </tr>`;
    } else {
        tbody.innerHTML = carrinho.map(item => `
            <tr>
                <td>${item.produtoNome}</td>
                <td>${item.quantidade}</td>
                <td>${formatBRL(item.precoUnitario)}</td>
                <td>${formatBRL(item.subtotal)}</td>
                <td>
                    <button class="btn btn-danger btn-sm" title="Remover"
                            onclick="removerItem(${item.produtoId})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>`).join('');
    }

    const totalItens = carrinho.reduce((s, i) => s + i.quantidade, 0);
    if (badgeItens) badgeItens.textContent = `${totalItens} ${totalItens === 1 ? 'item' : 'itens'}`;

    calcularTotais();
}

function calcularTotais() {
    const subtotal = carrinho.reduce((s, i) => s + i.subtotal, 0);
    const temDesconto = subtotal >= DESCONTO_MINIMO;
    const desconto = temDesconto ? subtotal * DESCONTO_PERCENTUAL : 0;
    const total = subtotal - desconto;

    const elSub = document.getElementById('totalCarrinho');
    const elDesc = document.getElementById('descontoAplicado');
    const elBadge = document.getElementById('badgeDesconto');
    const elTotal = document.getElementById('totalFinal');

    if (elSub) elSub.textContent = formatBRL(subtotal);
    if (elDesc) elDesc.textContent = formatBRL(desconto);
    if (elBadge) elBadge.style.display = temDesconto ? 'inline-block' : 'none';
    if (elTotal) elTotal.textContent = formatBRL(total);

    return { subtotal, desconto, total };
}

// ============================================================
//  FINALIZAR VENDA
// ============================================================

async function finalizarVenda() {
    const selCliente = document.getElementById('clienteVenda');
    const clienteId = parseInt(selCliente?.value);

    if (!clienteId) {
        mostrarToast('Selecione um cliente', 'warning');
        return;
    }
    if (carrinho.length === 0) {
        mostrarToast('Adicione ao menos um produto ao carrinho', 'warning');
        return;
    }

    const { desconto } = calcularTotais();

    const venda = {
        clienteId,
        desconto,
        itens: carrinho.map(i => ({
            produtoId: i.produtoId,
            quantidade: i.quantidade,
            precoUnitario: i.precoUnitario
        }))
    };

    const btn = document.getElementById('btnFinalizarVenda');
    if (btn) btn.disabled = true;

    try {
        await apiVendas.criar(venda);

        // Baixar o estoque dos produtos vendidos (no mock)
        for (const item of carrinho) {
            const prod = produtosCache.find(p => p.id === item.produtoId);
            if (prod) {
                const novoEstoque = Math.max(0, prod.estoque - item.quantidade);
                await apiProdutos.atualizar(prod.id, { estoque: novoEstoque });
            }
        }

        // Limpar carrinho e recarregar tudo
        carrinho = [];
        if (selCliente) selCliente.value = '';
        renderizarCarrinho();
        await carregarDadosVenda();
        await listarVendas();
        await carregarVendasAbertas();

    } catch (error) {
        console.error('Erro ao finalizar venda:', error);
        mostrarToast('Erro ao finalizar venda', 'danger');
    } finally {
        if (btn) btn.disabled = false;
    }
}

// ============================================================
//  HISTORICO DE VENDAS
// ============================================================

async function listarVendas() {
    const tbody = document.getElementById('tabelaVendas');
    if (!tbody) return;

    try {
        const vendas = await apiVendas.listar();

        if (!vendas || vendas.length === 0) {
            tbody.innerHTML = `
                <tr><td colspan="6" class="empty"><p>Nenhuma venda registrada.</p></td></tr>`;
            return;
        }

        // Mais recentes primeiro
        const ordenadas = [...vendas].sort((a, b) => new Date(b.dataVenda) - new Date(a.dataVenda));

        tbody.innerHTML = ordenadas.map(v => `
            <tr>
                <td>#${v.id}</td>
                <td>${v.clienteNome || '-'}</td>
                <td>${formatarDataVenda(v.dataVenda)}</td>
                <td>${(v.itens || []).length}</td>
                <td>${formatBRL(v.total)}</td>
                <td>${badgeStatus(v.status)}</td>
            </tr>`).join('');

    } catch (error) {
        console.error('Erro ao listar vendas:', error);
        tbody.innerHTML = `
            <tr><td colspan="6" class="empty"><p>Erro ao carregar vendas.</p></td></tr>`;
    }
}

// ============================================================
//  CANCELAR VENDA (apenas status ABERTA)
// ============================================================

async function carregarVendasAbertas() {
    const sel = document.getElementById('vendaIdCancelar');
    if (!sel) return;

    try {
        const vendas = await apiVendas.listar();
        const abertas = vendas.filter(v => v.status === 'ABERTA');

        if (abertas.length === 0) {
            sel.innerHTML = '<option value="">Nenhuma venda aberta disponivel</option>';
            return;
        }

        sel.innerHTML = '<option value="">Selecione...</option>';
        abertas.forEach(v => {
            const opt = document.createElement('option');
            opt.value = v.id;
            opt.textContent = `Venda #${v.id} - ${v.clienteNome} - ${formatBRL(v.total)}`;
            sel.appendChild(opt);
        });
    } catch (error) {
        console.error('Erro ao carregar vendas abertas:', error);
    }
}

async function cancelarVenda() {
    const sel = document.getElementById('vendaIdCancelar');
    const vendaId = parseInt(sel?.value);

    if (!vendaId) {
        mostrarToast('Selecione uma venda para cancelar', 'warning');
        return;
    }

    if (!confirm(`Cancelar a venda #${vendaId}? O estoque sera revertido.`)) return;

    try {
        const venda = await apiVendas.cancelar(vendaId);

        // Reverter estoque dos itens cancelados
        if (venda && Array.isArray(venda.itens)) {
            for (const item of venda.itens) {
                const prod = await apiProdutos.buscar(item.produtoId);
                if (prod) {
                    await apiProdutos.atualizar(prod.id, {
                        estoque: (prod.estoque || 0) + item.quantidade
                    });
                }
            }
        }

        await listarVendas();
        await carregarVendasAbertas();
        await carregarDadosVenda();

    } catch (error) {
        console.error('Erro ao cancelar venda:', error);
        mostrarToast('Esta venda nao pode ser cancelada', 'danger');
    }
}

// ============================================================
//  MODAL - NOVO CLIENTE
// ============================================================

let clienteModalCallback = null;

function abrirModalNovoCliente() {
    document.getElementById('novoClienteNome').value = '';
    document.getElementById('novoClienteCpf').value = '';
    document.getElementById('novoClienteEmail').value = '';
    document.getElementById('novoClienteTelefone').value = '';

    ['novoClienteNome', 'novoClienteCpf', 'novoClienteEmail'].forEach(id => {
        document.getElementById(id).classList.remove('error');
    });
    ['err-novo-cliente-nome', 'err-novo-cliente-cpf', 'err-novo-cliente-email'].forEach(id => {
        document.getElementById(id).classList.remove('visible');
    });

    document.getElementById('modalNovoCliente').classList.add('visible');
}

function fecharModalNovoCliente() {
    document.getElementById('modalNovoCliente').classList.remove('visible');
}

async function salvarNovoCliente() {
    const nome = document.getElementById('novoClienteNome').value.trim();
    const cpf = document.getElementById('novoClienteCpf').value.trim();
    const email = document.getElementById('novoClienteEmail').value.trim();
    const telefone = document.getElementById('novoClienteTelefone').value.trim();

    let valido = true;

    ['novoClienteNome', 'novoClienteCpf', 'novoClienteEmail'].forEach(id => {
        document.getElementById(id).classList.remove('error');
    });
    ['err-novo-cliente-nome', 'err-novo-cliente-cpf', 'err-novo-cliente-email'].forEach(id => {
        document.getElementById(id).classList.remove('visible');
    });

    if (!nome) {
        document.getElementById('novoClienteNome').classList.add('error');
        document.getElementById('err-novo-cliente-nome').classList.add('visible');
        valido = false;
    }

    const cpfNum = cpf.replace(/\D/g, '');
    if (cpfNum.length !== 11) {
        document.getElementById('novoClienteCpf').classList.add('error');
        document.getElementById('err-novo-cliente-cpf').textContent = 'CPF invalido (11 digitos)';
        document.getElementById('err-novo-cliente-cpf').classList.add('visible');
        valido = false;
    }

    const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRe.test(email)) {
        document.getElementById('novoClienteEmail').classList.add('error');
        document.getElementById('err-novo-cliente-email').classList.add('visible');
        valido = false;
    }

    if (!valido) return;

    try {
        const cliente = { nome, cpf: cpfNum, email, telefone, endereco: '' };
        const response = await apiClientes.criar(cliente);

        fecharModalNovoCliente();
        await carregarDadosVenda();

        const selectCliente = document.getElementById('clienteVenda');
        if (selectCliente) selectCliente.value = response.id;

        mostrarToast('Cliente cadastrado com sucesso!', 'success');

    } catch (error) {
        console.error('Erro ao criar cliente:', error);
        const msg = error.response?.data?.message || 'Erro ao cadastrar cliente';
        mostrarToast(msg, 'danger');
        if (error.response?.status === 409) {
            document.getElementById('novoClienteCpf').classList.add('error');
            document.getElementById('err-novo-cliente-cpf').textContent = 'CPF ja cadastrado';
            document.getElementById('err-novo-cliente-cpf').classList.add('visible');
        }
    }
}

// Fechar modal clicando fora
document.addEventListener('DOMContentLoaded', function () {
    const modal = document.getElementById('modalNovoCliente');
    if (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === this) fecharModalNovoCliente();
        });
    }
});

// ============================================================
//  EXPORTACOES GLOBAIS
// ============================================================

window.carregarDadosVenda = carregarDadosVenda;
window.configurarEventosVendas = configurarEventosVendas;
window.adicionarItem = adicionarItem;
window.removerItem = removerItem;
window.finalizarVenda = finalizarVenda;
window.listarVendas = listarVendas;
window.carregarVendasAbertas = carregarVendasAbertas;
window.cancelarVenda = cancelarVenda;
window.abrirModalNovoCliente = abrirModalNovoCliente;
window.fecharModalNovoCliente = fecharModalNovoCliente;
window.salvarNovoCliente = salvarNovoCliente;

console.log('✅ vendas.js carregado com sucesso!');
