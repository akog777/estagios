const API_BASE_URL = 'http://localhost:8080';

export const getVagas = async () => {
  const response = await fetch(`${API_BASE_URL}/vagas`);
  if (!response.ok) {
    throw new Error('Falha ao buscar vagas');
  }
  return await response.json();
};

export const login = async (email, senha) => {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, senha }),
  });
  if (!response.ok) {
    throw new Error('Falha no login');
  }
  return await response.json();
};

export const registrarEstudante = async (estudante) => {
  const response = await fetch(`${API_BASE_URL}/estudantes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(estudante),
  });
  if (!response.ok) {
    throw new Error('Falha ao registrar estudante');
  }
  return await response.json();
};

export const registrarEmpresa = async (empresa) => {
  const response = await fetch(`${API_BASE_URL}/empresas/registrar`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(empresa),
  });
  if (!response.ok) {
    throw new Error('Falha ao registrar empresa');
  }
  return await response.json();
};

export const getVagasRecomendadas = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch(
    `${API_BASE_URL}/estudantes/me/vagas-recomendadas`,
    {
      headers: { Authorization: `Bearer ${token}` },
    },
  );
  if (!response.ok) {
    throw new Error('Falha ao buscar vagas recomendadas');
  }
  return await response.json();
};

export const getMinhasVagas = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE_URL}/empresas/me/vagas`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    throw new Error('Falha ao buscar minhas vagas');
  }
  return await response.json();
};

export const getDashboardStats = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE_URL}/admins/dashboard`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    throw new Error('Falha ao buscar estatísticas');
  }
  return await response.json();
};

export const getMyInscricoes = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE_URL}/inscricoes/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    throw new Error('Falha ao buscar inscrições');
  }
  return await response.json();
};

export const getMyCandidates = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE_URL}/empresas/me/candidatos`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    throw new Error('Falha ao buscar candidatos');
  }
  return await response.json();
};
