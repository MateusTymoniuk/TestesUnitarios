package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LocacaoBuilder {

    private Locacao locacao;

    private LocacaoBuilder() { }

    public static LocacaoBuilder umaLocacao() {
        LocacaoBuilder builder = new LocacaoBuilder();
        builder.locacao = new Locacao();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().construir());
        builder.locacao.setFilmes(filmes);
        builder.locacao.setDataLocacao(new Date());
        builder.locacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(1));
        builder.locacao.setValor(4.0);
        return builder;
    }

    public LocacaoBuilder comDataRetorno(Date dataRetorno) {
        locacao.setDataRetorno(dataRetorno);
        return this;
    }

    public LocacaoBuilder comUsuario(Usuario usuario) {
        locacao.setUsuario(usuario);
        return this;
    }

    public LocacaoBuilder comAtraso() {
        locacao.setDataLocacao(DataUtils.obterDataComDiferencaDias(-4));
        locacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(-3));
        return this;
    }

    public Locacao construir() {
        return locacao;
    }
}
