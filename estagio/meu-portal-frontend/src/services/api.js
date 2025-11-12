const API_BASE_URL = 'http://localhost:8080';

export const getVagas = async () => {
    const response = await fetch(`${API_BASE_URL}/vagas`);
    if (!response.ok) {
        throw new Error('Falha ao buscar vagas');
    }
    return await response.json();
};
