// ============================================================
//  MÓDULO DE LISTAGEM E CANCELAMENTO DE VENDAS
//  Arquivo: pdv-lista.js
// ============================================================

// ===== ESTADO =====
let vendasRegistradas = [];
let vendasFiltradas = [];

// ===== CARREGAR VENDAS DA API =====
async function carregarVendas() {
    const container = document.getElementById('lista-vendas-container');
    if (!container) return;

    try {
        // Tenta buscar da API
        const token = localStorage.getItem('token');
        const response = await fetch('http://localhost:8080/vendas', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            }
        });

        if (response.ok) {
            const data = await response.json();
            if (data && data.length > 0) {
                vendasRegistradas = data;
                salvarVendasLocal();
                renderizarListaVendas();
                return;
            }
        }
        
        // Fallback: carregar do localStorage
        carregarVendasLocal();
        renderizarListaVendas();

    } catch (error) {
        console.warn('Erro ao carregar vendas da API, usando dados locais:', error);
        carregarVendasLocal();
        renderizarListaVendas();
    }
}

// ===== CARREGAR VENDAS DO LOCALSTORAGE =====
function carregarVendasLocal() {
    const vendasSalvas = localStorage.getItem('vendasRegistradas');
    if (vendasSalvas) {
        vendasRegistradas = JSON.parse(vendasSalvas);
    } else {
        // Dados de exemplo para teste
        vendasRegistradas = [
            {
                id: 1001,
                clienteId: 1,
                clienteNome: "Bruce Wayne",
                formaPagamento: "pix",
                itens: [
                    { produtoId: 101, nome: "Naruto Vol. 01", quantidade: 2, precoUnitario: 35.00 },
                    { produtoId: 102, nome: "O Senhor dos Anéis", quantidade: 1, precoUnitario: 79.90 }
                ],
                total: 149.90,
                status: 'ABERTA',
                data: new Date().toISOString()
            },
            {
                id: 1002,
                clienteId: 2,
                clienteNome: "Peter Parker",
                formaPagamento: "credito",
                itens: [
                    { produtoId: 103, nome: "Action Figure Goku", quantidade: 1, precoUnitario: 250.00 }
                ],
                total: 250.00,
                status: 'CANCELADA',
                data: new Date(Date.now() - 86400000).toISOString() // 1 dia atrás
            }
        ];
        salvarVendasLocal();
    }
}

// ===== SALVAR VENDAS NO LOCALSTORAGE =====
function salvarVendasLocal() {
    localStorage.setItem('vendasRegistradas', JSON.stringify(vendasRegistradas));
}

// ===== RENDERIZAR LISTA DE VENDAS =====
function renderizarListaVendas() {
    const container = document.getElementById('lista-vendas-container');
    if (!container) return;

    // Aplica filtros
    aplicarFiltros();

    if (vendasFiltradas.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: var(--muted);">
                <i class="bi bi-receipt" style="font-size: 48px; display: block; margin-bottom: 16px; opacity: 0.3;"></i>
                <h3 style="margin-bottom: 8px; color: var(--text);">Nenhuma venda encontrada</h3>
                <p style="font-size: 14px;">Realize uma venda para começar a listar</p>
            </div>
        `;
        return;
    }

    // Estatísticas
    const totalAbertas = vendasRegistradas.filter(v => v.status === 'ABERTA').length;
    const totalCanceladas = vendasRegistradas.filter(v => v.status === 'CANCELADA').length;

    let html = `
        <!-- Estatísticas -->
        <div class="vendas-stats" style="display: flex; gap: 20px; padding: 12px 0 20px; font-size: .9rem; flex-wrap: wrap; border-bottom: 1px solid var(--border); margin-bottom: 16px;">
            <div class="stat-item stat-total">
                <span>📊 Total:</span>
                <span class="numero" style="color: var(--blue);">${vendasRegistradas.length}</span>
            </div>
            <div class="stat-item stat-aberta">
                <span>🟢 Abertas:</span>
                <span class="numero" style="color: var(--success);">${totalAbertas}</span>
            </div>
            <div class="stat-item stat-cancelada">
                <span>🔴 Canceladas:</span>
                <span class="numero" style="color: var(--danger);">${totalCanceladas}</span>
            </div>
            <div style="margin-left: auto; color: var(--muted); font-size: .8rem;">
                Mostrando ${vendasFiltradas.length} de ${vendasRegistradas.length}
            </div>
        </div>

        <!-- Tabela -->
        <div class="table-wrap" style="overflow-x: auto;">
            <table>
                <thead>
                    <tr>
                        <th style="width: 80px;">#</th>
                        <th style="min-width: 150px;">Cliente</th>
                        <th style="min-width: 130px;">Data</th>
                        <th style="min-width: 100px;">Itens</th>
                        <th style="min-width: 120px;">Total</th>
                        <th style="min-width: 120px;">Pagamento</th>
                        <th style="min-width: 120px;">Status</th>
                        <th style="min-width: 160px;">Ações</th>
                    </tr>
                </thead>
                <tbody>
    `;

    vendasFiltradas.forEach(venda => {
        const dataFormatada = new Date(venda.data).toLocaleString('pt-BR');
        const totalItens = venda.itens.reduce((acc, item) => acc + item.quantidade, 0);
        const isCancelada = venda.status === 'CANCELADA';
        
        html += `
            <tr class="${isCancelada ? 'venda-cancelada' : ''}">
                <td><strong>#${venda.id}</strong></td>
                <td>${venda.clienteNome}</td>
                <td>${dataFormatada}</td>
                <td>${totalItens} itens</td>
                <td><strong>R$ ${venda.total.toFixed(2)}</strong></td>
                <td><span class="badge-pagamento">${venda.formaPagamento.toUpperCase()}</span></td>
                <td>
                    <span class="status-badge ${isCancelada ? 'badge-danger' : 'badge-success'}">
                        ${isCancelada ? '🔴' : '🟢'} ${isCancelada ? 'Cancelada' : 'Aberta'}
                    </span>
                </td>
                <td>
                    <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                        <button class="btn btn-sm btn-secondary" onclick="visualizarVenda(${venda.id})" title="Ver detalhes">
                            <i class="bi bi-eye"></i>
                        </button>
                        ${!isCancelada ? `
                            <button class="btn btn-sm btn-danger" onclick="cancelarVenda(${venda.id})" title="Cancelar venda">
                                <i class="bi bi-x-circle"></i> Cancelar
                            </button>
                        ` : `
                            <span style="color: var(--muted); font-size: 12px; padding: 6px 10px; background: var(--surface2); border-radius: 4px;">
                                <i class="bi bi-check-circle"></i> Finalizada
                            </span>
                        `}
                    </div>
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

// ===== FILTRAR VENDAS =====
function aplicarFiltros() {
    const statusFiltro = document.getElementById('filtro-status')?.value || 'todos';
    const dataInicio = document.getElementById('filtro-data-inicio')?.value;
    const dataFim = document.getElementById('filtro-data-fim')?.value;
    const clienteFiltro = document.getElementById('filtro-cliente')?.value?.toLowerCase() || '';

    vendasFiltradas = vendasRegistradas.filter(venda => {
        // Filtro por status
        if (statusFiltro !== 'todos' && venda.status !== statusFiltro) {
            return false;
        }

        // Filtro por data
        if (dataInicio) {
            const dataVenda = new Date(venda.data).toISOString().split('T')[0];
            if (dataVenda < dataInicio) return false;
        }
        if (dataFim) {
            const dataVenda = new Date(venda.data).toISOString().split('T')[0];
            if (dataVenda > dataFim) return false;
        }

        // Filtro por cliente
        if (clienteFiltro && !venda.clienteNome.toLowerCase().includes(clienteFiltro)) {
            return false;
        }

        return true;
    });
}

function filtrarVendas() {
    renderizarListaVendas();
}

// ===== VISUALIZAR DETALHES DA VENDA =====
function visualizarVenda(id) {
    const venda = vendasRegistradas.find(v => v.id === id);
    if (!venda) {
        alert('Venda não encontrada');
        return;
    }

    // Monta detalhes da venda
    let detalhes = `📋 DETALHES DA VENDA #${venda.id}\n`;
    detalhes += `${'='.repeat(40)}\n`;
    detalhes += `Cliente: ${venda.clienteNome}\n`;
    detalhes += `Data: ${new Date(venda.data).toLocaleString('pt-BR')}\n`;
    detalhes += `Pagamento: ${venda.formaPagamento.toUpperCase()}\n`;
    detalhes += `Status: ${venda.status === 'ABERTA' ? '✅ ABERTA' : '❌ CANCELADA'}\n`;
    detalhes += `${'='.repeat(40)}\n`;
    detalhes += `ITENS:\n`;

    venda.itens.forEach((item, index) => {
        const subtotal = item.quantidade * item.precoUnitario;
        detalhes += `\n${index + 1}. ${item.nome}`;
        detalhes += `\n   Quantidade: ${item.quantidade}`;
        detalhes += `\n   Preço: R$ ${item.precoUnitario.toFixed(2)}`;
        detalhes += `\n   Subtotal: R$ ${subtotal.toFixed(2)}`;
    });

    detalhes += `\n${'='.repeat(40)}`;
    detalhes += `\nTOTAL: R$ ${venda.total.toFixed(2)}`;

    alert(detalhes);
}

// ===== CANCELAR VENDA =====
async function cancelarVenda(id) {
    const venda = vendasRegistradas.find(v => v.id === id);
    
    if (!venda) {
        alert("❌ Venda não encontrada.");
        return;
    }
    
    // Verifica se o status é ABERTA
    if (venda.status !== 'ABERTA') {
        alert("❌ Apenas vendas com status ABERTA podem ser canceladas.");
        return;
    }
    
    // Confirmação
    const confirmacao = confirm(
        `⚠️ CONFIRMAR CANCELAMENTO\n\n` +
        `Venda #${venda.id}\n` +
        `Cliente: ${venda.clienteNome}\n` +
        `Total: R$ ${venda.total.toFixed(2)}\n` +
        `Data: ${new Date(venda.data).toLocaleString('pt-BR')}\n\n` +
        `Esta ação irá:\n` +
        `✅ Reverter o estoque dos produtos\n` +
        `✅ Marcar a venda como CANCELADA\n\n` +
        `Deseja continuar?`
    );
    
    if (!confirmacao) return;

    try {
        // Tenta cancelar via API
        const token = localStorage.getItem('token');
        const response = await fetch(`http://localhost:8080/vendas/${id}/cancelar`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            }
        });

        if (!response.ok) {
            throw new Error(`Erro ao cancelar venda: ${response.status}`);
        }

        // Atualiza status da venda
        venda.status = 'CANCELADA';
        
        // Reverte estoque local
        venda.itens.forEach(item => {
            const produto = window.produtosPDVRef?.find(p => p.id === item.produtoId);
            if (produto && produto.estoque !== undefined) {
                produto.estoque += item.quantidade;
            }
        });

        salvarVendasLocal();
        
        // Atualiza interfaces
        renderizarListaVendas();
        if (typeof renderizarCarrinho === 'function') {
            renderizarCarrinho();
        }

        alert(`✅ Venda #${venda.id} cancelada com sucesso!\nEstoque revertido.`);

    } catch (error) {
        console.error('Erro ao cancelar venda:', error);
        
        // Fallback: cancelamento local
        if (confirm(`❌ Erro ao cancelar na API: ${error.message}\n\nDeseja cancelar localmente?`)) {
            venda.status = 'CANCELADA';
            
            venda.itens.forEach(item => {
                const produto = window.produtosPDVRef?.find(p => p.id === item.produtoId);
                if (produto && produto.estoque !== undefined) {
                    produto.estoque += item.quantidade;
                }
            });

            salvarVendasLocal();
            renderizarListaVendas();
            if (typeof renderizarCarrinho === 'function') {
                renderizarCarrinho();
            }
            alert(`✅ Venda #${venda.id} cancelada localmente!`);
        }
    }
}

// ===== ATUALIZAR LISTA =====
function atualizarListaVendas() {
    carregarVendas();
}

// ===== REGISTRAR VENDA (chamado pelo pdv.js) =====
function registrarVenda(vendaData) {
    const novaVenda = {
        id: vendaData.id || Date.now(),
        ...vendaData,
        status: 'ABERTA',
        data: new Date().toISOString()
    };
    
    vendasRegistradas.unshift(novaVenda);
    salvarVendasLocal();
    renderizarListaVendas();
    
    return novaVenda;
}

// ===== INICIALIZAR MÓDULO =====
document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('section-vendas')) {
        carregarVendas();
    }
});

// ===== EXPORTAR FUNÇÕES =====
if (typeof window !== 'undefined') {
    window.carregarVendas = carregarVendas;
    window.renderizarListaVendas = renderizarListaVendas;
    window.cancelarVenda = cancelarVenda;
    window.visualizarVenda = visualizarVenda;
    window.filtrarVendas = filtrarVendas;
    window.atualizarListaVendas = atualizarListaVendas;
    window.registrarVenda = registrarVenda;
    window.vendasRegistradas = vendasRegistradas;
}