'use client';

import React, { useState } from 'react';
import styles from './page.module.css';

export default function AuthPage() {
    // --- Estado para controlar a aba ativa ---
    const [activeTab, setActiveTab] = useState('login'); // 'login', 'student_register', 'company_register'

    // --- Estados para o formulário de Cadastro de Estudante ---
    const [nome, setNome] = useState('');
    const [cpf, setCpf] = useState('');
    const [curso, setCurso] = useState('');
    const [telefone, setTelefone] = useState('');

    // --- Estados para o formulário de Cadastro de Empresa ---
    const [nomeEmpresa, setNomeEmpresa] = useState('');
    const [cnpj, setCnpj] = useState('');
    const [telefoneEmpresa, setTelefoneEmpresa] = useState('');
    const [endereco, setEndereco] = useState('');

    // --- Estados para ambos os formulários ---
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');

    // --- Estados para feedback ao usuário ---
    const [mensagem, setMensagem] = useState('');
    const [erro, setErro] = useState('');

    // URL da sua API backend
    const API_URL = 'https://animated-adventure-x5vxvp45wr4vcr9p-8080.app.github.dev';

    const resetForm = () => {
        setNome(''); setCpf(''); setCurso(''); setTelefone('');
        setNomeEmpresa(''); setCnpj(''); setTelefoneEmpresa(''); setEndereco('');
        setEmail(''); setSenha('');
    };

    const handleTabChange = (tab) => {
        setActiveTab(tab);
        setMensagem('');
        setErro('');
        resetForm();
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setMensagem('');
        setErro('');

        try {
            // O endpoint de login geralmente espera um 'username' e 'password'
            // e retorna um token. A implementação exata depende do seu backend.
            // Este é um exemplo genérico.
            const response = await fetch(`${API_URL}/usuarios/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, senha }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Falha no login. Verifique suas credenciais.');
            }

            const data = await response.json(); // Supondo que o backend retorne um token
            // localStorage.setItem('token', data.token); // Salva o token
            setMensagem('Login realizado com sucesso! Redirecionando...');
            // Aqui você redirecionaria o usuário para o painel

        } catch (error) {
            setErro(error.message);
        }
    };

    const handleCadastroEstudante = async (e) => {
        e.preventDefault();
        setMensagem('');
        setErro('');

        const payload = { nome, cpf, email, senha, curso, telefone };

        try {
            const response = await fetch(`${API_URL}/estudantes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Ocorreu um erro ao cadastrar.');
            }

            setMensagem('Cadastro de estudante realizado com sucesso! Você já pode fazer login.');
            resetForm();
            setActiveTab('login');

        } catch (error) {
            setErro(error.message);
        }
    };

    const handleCadastroEmpresa = async (e) => {
        e.preventDefault();
        setMensagem('');
        setErro('');

        const payload = {
            nome: nomeEmpresa,
            cnpj,
            telefone: telefoneEmpresa,
            endereco,
            usuario: {
                email,
                senha
            }
        };

        try {
            const response = await fetch(`${API_URL}/empresas/registrar`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Ocorreu um erro ao cadastrar a empresa.');
            }

            setMensagem('Cadastro de empresa realizado com sucesso! Você já pode fazer login.');
            resetForm();
            setActiveTab('login');

        } catch (error) {
            setErro(error.message);
        }
    };

    const renderForm = () => {
        if (activeTab === 'login') {
            return (
                <form onSubmit={handleLogin} className={styles.form}>
                    <h2>Login</h2>
                    <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                    <input type="password" placeholder="Senha" value={senha} onChange={(e) => setSenha(e.target.value)} required />
                    <button type="submit">Entrar</button>
                </form>
            );
        }

        if (activeTab === 'student_register') {
            return (
                <form onSubmit={handleCadastroEstudante} className={styles.form}>
                    <h2>Cadastro de Estudante</h2>
                    <input type="text" placeholder="Nome Completo" value={nome} onChange={(e) => setNome(e.target.value)} required />
                    <input type="text" placeholder="CPF" value={cpf} onChange={(e) => setCpf(e.target.value)} required />
                    <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                    <input type="password" placeholder="Senha" value={senha} onChange={(e) => setSenha(e.target.value)} required />
                    <input type="text" placeholder="Curso" value={curso} onChange={(e) => setCurso(e.target.value)} />
                    <input type="tel" placeholder="Telefone" value={telefone} onChange={(e) => setTelefone(e.target.value)} />
                    <button type="submit">Cadastrar</button>
                </form>
            );
        }

        if (activeTab === 'company_register') {
            return (
                <form onSubmit={handleCadastroEmpresa} className={styles.form}>
                    <h2>Cadastro de Empresa</h2>
                    <input type="text" placeholder="Nome da Empresa" value={nomeEmpresa} onChange={(e) => setNomeEmpresa(e.target.value)} required />
                    <input type="text" placeholder="CNPJ" value={cnpj} onChange={(e) => setCnpj(e.target.value)} required />
                    <input type="email" placeholder="Email de Contato" value={email} onChange={(e) => setEmail(e.target.value)} required />
                    <input type="password" placeholder="Senha" value={senha} onChange={(e) => setSenha(e.target.value)} required />
                    <input type="tel" placeholder="Telefone" value={telefoneEmpresa} onChange={(e) => setTelefoneEmpresa(e.target.value)} />
                    <input type="text" placeholder="Endereço" value={endereco} onChange={(e) => setEndereco(e.target.value)} />
                    <button type="submit">Cadastrar</button>
                </form>
            );
        }
    };

    return (
        <main className={styles.main}>
            <div className={styles.formWrapper}>
                <div className={styles.tabContainer}>
                    <button
                        className={`${styles.tab} ${activeTab === 'login' ? styles.activeTab : ''}`}
                        onClick={() => handleTabChange('login')}
                    >
                        Login
                    </button>
                    <button
                        className={`${styles.tab} ${activeTab === 'student_register' ? styles.activeTab : ''}`}
                        onClick={() => handleTabChange('student_register')}
                    >
                        Sou Estudante
                    </button>
                    <button
                        className={`${styles.tab} ${activeTab === 'company_register' ? styles.activeTab : ''}`}
                        onClick={() => handleTabChange('company_register')}
                    >
                        Sou Empresa
                    </button>
                </div>

                {renderForm()}

                {mensagem && (
                    <div className={`${styles.messages} ${styles.success}`}>
                        {mensagem}
                    </div>
                )}
                {erro && (
                    <div className={`${styles.messages} ${styles.error}`}>
                        {erro}
                    </div>
                )}
            </div>
        </main>
    );
}