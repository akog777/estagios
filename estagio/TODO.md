# TODO - Correções para Portal de Estágios

## Backend
- [x] Remover endpoint duplicado `/usuarios/login` do UsuarioController
- [x] Criar endpoint público `/empresas/registrar` para cadastro de empresas
- [x] Restringir endpoints `/areas-interesse` para `ROLE_ADMIN` no SecurityConfig
- [x] Ajustar SecurityConfig para permitir cadastros públicos (/estudantes POST, /empresas/registrar POST)
- [x] Verificar entidade VagaEstagio (remover campos extras se não necessários)
- [x] Confirmar status inicial de vagas como "ABERTA"

## Frontend
- [x] Corrigir chamadas de API em page.js para usar `/auth/login` e `/empresas/registrar`
- [x] Implementar redirecionamento após login baseado no role (estudante, empresa, admin)
- [x] Criar painel para estudantes (ver vagas recomendadas, inscrições)
- [x] Criar painel para empresas (gerenciar vagas, ver candidatos)
- [x] Criar painel para admins (dashboard com estatísticas e gráfico)
- [x] Adicionar componentes para CRUD de vagas, inscrições, etc. (painéis básicos implementados)
- [x] Implementar gráfico no dashboard admin usando dados de `/admins/dashboard`

## Geral
- [x] Testar integração backend-frontend (build e lint passaram)
- [x] Verificar segurança dos endpoints (JWT, roles configurados)
- [ ] Confirmar funcionalidade inovadora (PDF) acessível
