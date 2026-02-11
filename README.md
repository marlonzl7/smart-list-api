# Smart List API

API REST responsável pelo backend da aplicação **Smart List**.
Fornece autenticação, regras de negócio e persistência de dados para o gerenciamento de inventário doméstico e geração automática de listas de compras com base no consumo do usuário.

Este projeto faz parte do ecossistema **Smart List**.

- Visão geral do projeto: https://github.com/marlonzl7/smart-list
- Frontend Web: https://github.com/marlonzl7/smart-list-web

---

## Versão atual

**v1.0.7**

Versão estável atual da Smart List API.  
As versões anteriores da série **1.0.x** focaram em correções incrementais, incluindo ajustes de inicialização, estabilidade e pequenos refinamentos internos.

---

## Release v1.0.0 — MVP funcional

A versão **v1.0.0** marcou a entrega do **MVP funcional** da Smart List API.

Essa release consolidou todas as funcionalidades essenciais do sistema, estabelecendo a base do projeto.  
As versões subsequentes da série **1.0.x** não alteraram o escopo funcional do MVP, focando exclusivamente em correções e melhorias de estabilidade.

---

## Funcionalidades implementadas

- Cadastro de usuários
- Autenticação stateless com **JWT**
- Uso de **Access Token** e **Refresh Token com rotação**
- Logout com invalidação de Refresh Token
- Recuperação de senha via email
- CRUD de categorias do inventário
- CRUD de itens do inventário do usuário
- Listagem de unidades de medida
- Criação automática da lista de compras com base no consumo diário
- Finalização da lista de compras com compensação automática de estoque
- Testes unitários cobrindo os principais serviços

---

## Funcionalidades planejadas

- Atualização parcial de atributos do usuário (PATCH)
- Envio de notificações por email e/ou WhatsApp quando um item entrar na lista de compras

---

## Endpoints

### Usuário

- **POST** `/users/register`  
  Cadastro de usuário

---

### Autenticação

- **POST** `/auth/login`  
  Autenticação do usuário

- **POST** `/auth/refresh`  
  Atualização do Access Token via Refresh Token

- **POST** `/auth/logout`  
  Invalidação do Refresh Token

---

### Inventário

#### Categorias

- **GET** `/inventory/categories`  
  Lista categorias do usuário (paginado)

- **GET** `/inventory/categories/all`  
  Lista todas as categorias disponíveis

- **POST** `/inventory/categories`  
  Cadastro de categoria

- **PATCH** `/inventory/categories/{id}`  
  Atualiza uma categoria

- **DELETE** `/inventory/categories/{id}`  
  Deleta uma categoria

---

#### Itens

- **GET** `/inventory/items`  
  Lista itens do usuário (paginado)

- **POST** `/inventory/items`  
  Cadastro de item

- **PATCH** `/inventory/items/{id}`  
  Atualiza um item

- **DELETE** `/inventory/items/{id}`  
  Deleta um item

---

#### Unidades de medida

- **GET** `/inventory/items/units`  
  Lista todas as unidades de medida disponíveis

---

### Redefinição de senha

- **POST** `/password-resets/request`  
  Solicita redefinição de senha

- **GET** `/password-resets/validate`  
  Valida o token de redefinição de senha

- **POST** `/password-resets/confirm`  
  Confirma a redefinição e cria uma nova senha

---

### Lista de Compras

- **GET** `/shopping-lists/active`  
  Obtém a lista de compras ativa do usuário

- **GET** `/shopping-lists/{id}`  
  Obtém uma lista de compras específica do usuário

- **PATCH** `/shopping-lists/items/{id}`  
  Atualiza um item da lista de compras

- **DELETE** `/shopping-lists/{id}`  
  Remove um item da lista de compras

- **POST** `/shopping-lists/{id}/finalize`  
  Finaliza a lista de compras e atualiza o estoque

---

## Observações técnicas

- Arquitetura baseada em **API REST**
- Autenticação **stateless** com JWT
- Refresh Token persistido em banco de dados
- Rotação de Refresh Token para maior segurança
- Organização do projeto por **domínios**
- Scripts SQL manuais para controle do schema
- Banco de dados: **PostgreSQL**
- Backend desenvolvido em **Java com Spring Boot**

---

## Status do projeto

A Smart List API encontra-se em estado **estável**, representando o MVP funcional do sistema.
O projeto segue em evolução, com melhorias e novas funcionalidades planejadas para versões futuras.
