import VagaList from '@/components/VagaList'; // Usando o alias!

export default function HomePage() {
  return (
    <main className="max-w-4xl mx-auto p-8">
      <header className="text-center mb-12">
        <h1 className="text-4xl font-bold text-blue-800">Portal de Estágios</h1>
      </header>
      <VagaList />
    </main>
  );
}
