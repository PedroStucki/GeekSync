# GeekSync ERP — Frontend (modo demonstração / sem backend)

Frontend em HTML/CSS/JS puro. Roda 100% no navegador usando dados **mock**
(em memória) — o backend Spring Boot será integrado depois.

## Como rodar

Precisa de um servidor local (o `fetch` das páginas não funciona via `file://`).
A partir desta pasta:

```bash
python -m http.server 5500
```

Abra: http://localhost:5500/  (ou direto em /assets/pages/index.html)

Login: **admin / 123456**  (também: gerente / 123456 e vendedor / 123456)

## O que funciona sem backend

- Login (3 perfis mock)
- Dashboard (cards + gráfico de vendas)
- Clientes e Produtos (CRUD completo)
- Vendas (carrinho, desconto, finalizar, histórico, cancelar venda aberta)
- Relatórios

> Os dados são em memória: recarregar a página volta ao estado inicial de exemplo.
> O desconto de 10% é aplicado para subtotal ≥ R$ 300 (ajustável em `assets/js/vendas.js`, constante `DESCONTO_MINIMO`).

## Integrar o backend depois

No final de `assets/js/api.js` há o bloco de mock (Object.assign dos *Mock).
Remova/comente esse bloco para voltar a usar as chamadas reais à API
(`http://localhost:8080/api`).

## Observação

`assets/js/pdv.js` e `assets/pages/pdv.html` são uma tela de PDV experimental,
ainda não ligada ao menu. Não afeta o restante do sistema.
