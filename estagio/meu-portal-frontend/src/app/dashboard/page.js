'use client';

import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Title,
  Tooltip,
} from 'chart.js';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { Bar } from 'react-chartjs-2';
import {
  getDashboardStats,
  getMinhasVagas,
  getMyCandidates,
  getMyInscricoes,
  getVagasRecomendadas,
} from '../../services/api';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
);

export default function Dashboard() {
  const [_user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/');
      return;
    }

    // Decodificar JWT para obter role (simplificado)
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      setRole(payload.role); // Assumindo que o token tem 'role'
      setUser(payload);
    } catch (error) {
      console.error('Erro ao decodificar token:', error);
      localStorage.removeItem('token');
      router.push('/');
    }
  }, [router]);

  const logout = () => {
    localStorage.removeItem('token');
    router.push('/');
  };

  if (!role) return <div>Carregando...</div>;

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-4xl mx-auto bg-white p-6 rounded-lg shadow-md">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <button
            type="button"
            onClick={logout}
            className="bg-red-500 text-white px-4 py-2 rounded"
          >
            Logout
          </button>
        </div>

        {role === 'ROLE_ESTUDANTE' && <StudentDashboard />}
        {role === 'ROLE_EMPRESA' && <CompanyDashboard />}
        {role === 'ROLE_ADMIN' && <AdminDashboard />}
      </div>
    </div>
  );
}

function StudentDashboard() {
  const [vagas, setVagas] = useState([]);
  const [inscricoes, setInscricoes] = useState([]);

  useEffect(() => {
    getVagasRecomendadas().then(setVagas).catch(console.error);
    getMyInscricoes().then(setInscricoes).catch(console.error);
  }, []);

  return (
    <div>
      <h2 className="text-2xl mb-4">Vagas Recomendadas</h2>
      <div className="space-y-4 mb-8">
        {vagas.map((vaga) => (
          <div key={vaga.id} className="p-4 bg-gray-50 rounded">
            <h3 className="font-bold">{vaga.titulo}</h3>
            <p>{vaga.descricao}</p>
            <button
              type="button"
              className="mt-2 bg-blue-500 text-white px-4 py-2 rounded"
            >
              Inscrever-se
            </button>
          </div>
        ))}
      </div>
      <h2 className="text-2xl mb-4">Minhas Inscrições</h2>
      <div className="space-y-4">
        {inscricoes.map((inscricao) => (
          <div key={inscricao.id} className="p-4 bg-gray-50 rounded">
            <h3 className="font-bold">{inscricao.vagaEstagio.titulo}</h3>
            <p>Status: {inscricao.status}</p>
            <p>
              Data: {new Date(inscricao.dataInscricao).toLocaleDateString()}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}

function CompanyDashboard() {
  const [vagas, setVagas] = useState([]);
  const [candidatos, setCandidatos] = useState([]);

  useEffect(() => {
    getMinhasVagas().then(setVagas).catch(console.error);
    getMyCandidates().then(setCandidatos).catch(console.error);
  }, []);

  return (
    <div>
      <h2 className="text-2xl mb-4">Minhas Vagas</h2>
      <button
        type="button"
        className="mb-4 bg-green-500 text-white px-4 py-2 rounded"
      >
        Criar Nova Vaga
      </button>
      <div className="space-y-4 mb-8">
        {vagas.map((vaga) => (
          <div key={vaga.id} className="p-4 bg-gray-50 rounded">
            <h3 className="font-bold">{vaga.titulo}</h3>
            <p>{vaga.descricao}</p>
            <p>Status: {vaga.status}</p>
            <button
              type="button"
              className="mt-2 bg-yellow-500 text-white px-4 py-2 rounded"
            >
              Editar
            </button>
            <button
              type="button"
              className="mt-2 ml-2 bg-red-500 text-white px-4 py-2 rounded"
            >
              Encerrar
            </button>
          </div>
        ))}
      </div>
      <h2 className="text-2xl mb-4">Candidatos Inscritos</h2>
      <div className="space-y-4">
        {candidatos.map((candidato) => (
          <div key={candidato.id} className="p-4 bg-gray-50 rounded">
            <h3 className="font-bold">{candidato.estudante.nome}</h3>
            <p>Vaga: {candidato.vagaEstagio.titulo}</p>
            <p>Status: {candidato.status}</p>
            <p>
              Data: {new Date(candidato.dataInscricao).toLocaleDateString()}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}

function AdminDashboard() {
  const [stats, setStats] = useState({});

  useEffect(() => {
    getDashboardStats().then(setStats).catch(console.error);
  }, []);

  const chartData = {
    labels: stats.vagasPorArea
      ? stats.vagasPorArea.map((item) => item.area)
      : [],
    datasets: [
      {
        label: 'Vagas por Área',
        data: stats.vagasPorArea
          ? stats.vagasPorArea.map((item) => item.quantidade)
          : [],
        backgroundColor: 'rgba(54, 162, 235, 0.6)',
      },
    ],
  };

  return (
    <div>
      <h2 className="text-2xl mb-4">Dashboard Administrativo</h2>
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="p-4 bg-blue-100 rounded">
          <h3 className="font-bold">Empresas</h3>
          <p className="text-2xl">{stats.quantidadeEmpresas || 0}</p>
        </div>
        <div className="p-4 bg-green-100 rounded">
          <h3 className="font-bold">Estudantes</h3>
          <p className="text-2xl">{stats.quantidadeEstudantes || 0}</p>
        </div>
        <div className="p-4 bg-yellow-100 rounded">
          <h3 className="font-bold">Vagas Abertas</h3>
          <p className="text-2xl">{stats.vagasAbertas || 0}</p>
        </div>
        <div className="p-4 bg-red-100 rounded">
          <h3 className="font-bold">Vagas Encerradas</h3>
          <p className="text-2xl">{stats.vagasEncerradas || 0}</p>
        </div>
      </div>
      <div className="p-4 bg-gray-50 rounded">
        <h3 className="font-bold mb-2">Vagas por Área</h3>
        <Bar data={chartData} />
      </div>
    </div>
  );
}
