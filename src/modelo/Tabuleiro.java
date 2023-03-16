package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tabuleiro implements CampoObservador {
	private int linhas;
	private int colunas;
	private int minas;
	
	private final List<Campo> campos = new ArrayList<>();
	private final List<Consumer<Boolean>> observadores = new ArrayList<>();

	public Tabuleiro(int linhas, int colunas, int minas) {
		this.linhas = linhas;
		this.colunas = colunas;
		this.minas = minas;
		gerarCampo();
		associarVizinhos();
		sortearMinas();
	}
	public void registrarObservador(Consumer<Boolean> observador) {
		observadores.add(observador);
	}
	private void notificarObservadores(boolean resultado) {
		observadores.stream().forEach(obs -> obs.accept(resultado));
	}
	
	private void gerarCampo() {
		for(int i = 0 ; i < linhas;i++) {
			for(int j = 0; j < colunas; j++) {
				Campo campo = new Campo(i,j);
				campo.resgistrarObservador(this);
				campos.add(campo);
			}
		}
	}
	
	private void associarVizinhos() {
		for(Campo c1 : campos) {
			for (Campo c2 : campos) {
				c1.adicionarVizinho(c2);
			}
		}
	}
	
	private void sortearMinas() {
		long minasArmadas = 0;
		Predicate<Campo> minado = c -> c.isMinado();
		
		do {
			int aleatorio = (int) (Math.random() * campos.size());
			campos.get(aleatorio).minar();
			minasArmadas = campos.stream().filter(minado).count();
		}while(minasArmadas < minas);
	}
	
	public boolean objetivoAlcancado() {
		return campos.stream().allMatch(c -> c.objetivoAlcancado());
	}
	
	public void reiniciar() {
		campos.stream().forEach(c -> c.reiniciar());
		sortearMinas();
	}
	
	public void abrir(int linha, int coluna) {
			campos.parallelStream()
			.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
			.findFirst()
			.ifPresent(c -> c.abrir());
	}
	
	private void mostrarMinas() {
		campos.stream()
		.filter(c -> c.isMinado())
		.forEach(c -> c.setAberto(true));
	}
	
	public void alternarMarcacao(int linha, int coluna) {
		campos.parallelStream()
		.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
		.findFirst()
		.ifPresent(c -> c.alternarMarcacao());
	}
	
	public void eventoOcorreu(Campo campo,CampoEvento evento) {
		if(evento == CampoEvento.EXPLODIR) {
			mostrarMinas();
			notificarObservadores(false);
		}else if(objetivoAlcancado()) {
			System.out.println("Ganhouuu :)");
			notificarObservadores(true);
		}
	}
}
