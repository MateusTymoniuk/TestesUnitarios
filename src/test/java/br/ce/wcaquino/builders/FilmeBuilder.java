package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Filme;

public class FilmeBuilder {

	private Filme filme;
	
	private FilmeBuilder() { }
	
	public static FilmeBuilder umFilme() {
		FilmeBuilder builder = new FilmeBuilder();
		builder.filme = new Filme();
		builder.filme.setNome("Filme A");
		builder.filme.setEstoque(1);
		builder.filme.setPrecoLocacao(4.0);
		return builder;
	}
	
	public static FilmeBuilder umFilmeSemEstoque() {
		FilmeBuilder builder = new FilmeBuilder();
		builder.filme = new Filme();
		builder.filme.setNome("Filme A");
		builder.filme.setEstoque(0);
		builder.filme.setPrecoLocacao(4.0);
		return builder;
	}
	
	public FilmeBuilder comPrecoLocacao(Double preco) {
		filme.setPrecoLocacao(preco);
		return this;
	}
	
	public Filme construir() {
		return filme;
	}
}
