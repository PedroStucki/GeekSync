// assets/js/main.js
// Inicialização Principal do Sistema

// ============================================================
//  INICIALIZAÇÃO
// ============================================================

document.addEventListener('DOMContentLoaded', async () => {
    console.log('🚀 GeekSync ERP Inicializando...');

    // Verificar autenticação
    const token = localStorage.getItem('jwt_token');
    const usuario = getUsuarioLogado();

    if (token && usuario) {
        console.log('✅ Usuário autenticado:', usuario.nome);
        
        document.getElementById('page-login').classList.remove('active');
        document.getElementById('page-app').classList.add('active');
        
        document.getElementById('user-display-name').textContent = usuario.nome;
        document.getElementById('user-display-role').textContent = usuario.perfil;
        document.getElementById('user-avatar').textContent = usuario.nome[0];

        // Carregar páginas e dados
        await carregarTodasPaginas();
        await carregarDadosIniciais();
    } else {
        console.log('🔐 Usuário não autenticado');
        document.getElementById('page-login').classList.add('active');
        document.getElementById('page-app').classList.remove('active');
    }

    // Configurar evento de Enter no login
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && document.getElementById('page-login').classList.contains('active')) {
            doLogin();
        }
    });
});

// ============================================================
//  CARREGAR PÁGINAS
// ============================================================

async function carregarTodasPaginas() {
    const paginas = ['dashboard', 'clientes', 'produtos', 'pdv', 'vendas', 'relatorios'];
    
    for (const pagina of paginas) {
        await carregarPagina(pagina);
    }
}

async function carregarPagina(section) {
    try {
        console.log(`📄 Carregando página: ${section}`);
        
        // 🔥 CAMINHO CORRETO
        const response = await fetch(`pages/${section}.html`);
        
        // 🔥 LOG PARA DEBUG
        console.log(`🔍 Buscando: pages/${section}.html`);
        console.log(`📊 Status: ${response.status}`);
        
        if (!response.ok) {
            console.warn(`⚠️ Página ${section}.html não encontrada (status: ${response.status})`);
            const container = document.getElementById(`${section}-content`);
            if (container) {
                container.innerHTML = `
                    <div class="empty">
                        <p>⚠️ Página ${section} não encontrada.</p>
                        <p style="font-size:.8rem; color:var(--muted);">
                            Verifique se o arquivo <strong>pages/${section}.html</strong> existe.
                        </p>
                        <p style="font-size:.8rem; color:var(--muted);">
                            Caminho atual: <strong>${window.location.pathname}</strong>
                        </p>
                    </div>
                `;
            }
            return;
        }
        
        const html = await response.text();
        const container = document.getElementById(`${section}-content`);
        if (container) {
            container.innerHTML = html;
            console.log(`✅ Página ${section} carregada`);
        }

        await inicializarModulo(section);

    } catch (error) {
        console.error(`❌ Erro ao carregar página ${section}:`, error);
        const container = document.getElementById(`${section}-content`);
        if (container) {
            container.innerHTML = `
                <div class="empty">
                    <p>❌ Erro ao carregar página ${section}.</p>
                    <p style="font-size:.8rem; color:var(--danger);">
                        ${error.message}
                    </p>
                </div>
            `;
        }
    }
}

// ============================================================
//  INICIALIZAR MÓDULOS
// ============================================================

async function inicializarModulo(section) {
    console.log(`🔧 Inicializando módulo: ${section}`);
    
    switch(section) {
        case 'dashboard':
            if (typeof carregarDashboard === 'function') {
                await carregarDashboard();
                console.log('✅ Dashboard inicializado');
            } else {
                console.warn('⚠️ carregarDashboard não está definido');
            }
            break;
            
        case 'clientes':
            if (typeof carregarClientes === 'function') {
                await carregarClientes();
                console.log('✅ Clientes inicializado');
            } else {
                console.warn('⚠️ carregarClientes não está definido');
            }
            break;
            
        case 'produtos':
            if (typeof carregarProdutos === 'function') {
                await carregarProdutos();
                console.log('✅ Produtos inicializado');
            } else {
                console.warn('⚠️ carregarProdutos não está definido');
            }
            break;
            
        case 'pdv':
            if (typeof carregarDadosPDV === 'function') {
                await carregarDadosPDV();
                console.log('✅ PDV inicializado');
            } else {
                console.warn('⚠️ carregarDadosPDV não está definido');
            }
            break;
            
        case 'vendas':
            if (typeof carregarDadosVenda === 'function') {
                await carregarDadosVenda();
                console.log('✅ Dados de venda carregados');
            }
            if (typeof carregarVendasAbertas === 'function') {
                await carregarVendasAbertas();
                console.log('✅ Vendas abertas carregadas');
            }
            if (typeof listarVendas === 'function') {
                await listarVendas();
                console.log('✅ Histórico de vendas carregado');
            }
            if (typeof configurarEventosVendas === 'function') {
                configurarEventosVendas();
                console.log('✅ Eventos de vendas configurados');
            }
            console.log('✅ Vendas inicializado');
            break;
            
        case 'relatorios':
            if (typeof inicializarRelatorios === 'function') {
                await inicializarRelatorios();
                console.log('✅ Relatórios inicializado');
            } else {
                console.warn('⚠️ inicializarRelatorios não está definido');
            }
            break;
    }
}

// ============================================================
//  CARREGAR DADOS INICIAIS
// ============================================================

async function carregarDadosIniciais() {
    try {
        console.log('📊 Carregando dados iniciais...');
        
        const promises = [];
        
        if (typeof carregarClientes === 'function') {
            promises.push(carregarClientes());
        }
        
        if (typeof carregarProdutos === 'function') {
            promises.push(carregarProdutos());
        }
        
        if (promises.length > 0) {
            await Promise.all(promises);
            console.log('✅ Dados iniciais carregados');
        }
        
    } catch (error) {
        console.error('❌ Erro ao carregar dados iniciais:', error);
    }
}

// ============================================================
//  EXPORTAÇÕES GLOBAIS
// ============================================================

window.carregarPagina = carregarPagina;
window.carregarTodasPaginas = carregarTodasPaginas;
window.carregarDadosIniciais = carregarDadosIniciais;
window.inicializarModulo = inicializarModulo;

console.log('✅ main.js carregado com sucesso!');