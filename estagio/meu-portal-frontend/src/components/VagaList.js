'use client'; // Necessário para usar hooks como useState e useEffect no App Router

import { useState, useEffect } from 'react';
import { getVagas } from '@/services/api'; // Usando o alias!

export default function VagaList() {
    const [vagas, setVagas] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchVagas = async () => {
            try {
                const data = await getVagas();
                setVagas(data);
            } catch (err) {
                setError(err.message);
            }
        };

        fetchVagas();
    }, []);

    if (error) {
        return <p className="text-red-500">Erro ao carregar vagas: {error}</p>;
    }

    return (
        <div className="space-y-4">
            {vagas.map(vaga => (
                <div key={vaga.id} className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm">
                    <h2 className="text-2xl font-bold text-blue-700">{vaga.titulo}</h2>
                    <p className="mt-2 text-gray-600">{vaga.descricao}</p>
                    <p className="mt-4 text-sm text-gray-500">{vaga.empresa.nome} - {vaga.localizacao}</p>
                </div>
            ))}
        </div>
    );
}
