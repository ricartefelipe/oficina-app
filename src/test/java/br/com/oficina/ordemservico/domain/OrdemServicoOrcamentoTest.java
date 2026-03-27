package br.com.oficina.ordemservico.domain;

import br.com.oficina.cadastros.cliente.domain.Cliente;
import br.com.oficina.cadastros.cliente.domain.CpfCnpj;
import br.com.oficina.cadastros.veiculo.domain.Placa;
import br.com.oficina.cadastros.veiculo.domain.Veiculo;
import br.com.oficina.catalogo.peca.domain.PecaInsumo;
import br.com.oficina.catalogo.servico.domain.ServicoCatalogo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoOrcamentoTest {

    @Test
    void deveCalcularOrcamentoComoSomaDeServicosMaisPecas() {
        Cliente cliente = Cliente.novo("Joao", CpfCnpj.of("529.982.247-25"));
        Veiculo veiculo = Veiculo.novo(Placa.of("ABC-1234"), "VW", "Gol", 2020, cliente);

        OrdemServico os = OrdemServico.receber(cliente, veiculo, "TRACK1234");

        ServicoCatalogo servico = ServicoCatalogo.novo("Troca de oleo", null, new BigDecimal("150.00"), 60);
        PecaInsumo peca = PecaInsumo.nova("Filtro", null, new BigDecimal("35.00"), 10, 2);

        os.adicionarServico(servico, 1);
        os.adicionarPeca(peca, 2);

        BigDecimal esperado = new BigDecimal("220.00");
        assertEquals(0, esperado.compareTo(os.getOrcamentoTotal()));
        assertEquals(2, os.getOrcamentoTotal().scale());
    }

    @Test
    void deveIniciarComStatusRecebidaERegistrarHistoricoInicial() {
        Cliente cliente = Cliente.novo("Maria", CpfCnpj.of("111.444.777-35"));
        Veiculo veiculo = Veiculo.novo(Placa.of("ABC1D23"), "Fiat", "Uno", 2018, cliente);

        OrdemServico os = OrdemServico.receber(cliente, veiculo, "ABCDEFGH");

        assertEquals(StatusOrdemServico.RECEBIDA, os.getStatus());
        assertFalse(os.getTransicoesStatus().isEmpty());
        assertEquals(StatusOrdemServico.RECEBIDA, os.getTransicoesStatus().get(0).getParaStatus());
    }

    @Test
    void recusarOrcamentoEmAguardandoVaiParaCancelada() {
        Cliente cliente = Cliente.novo("Maria", CpfCnpj.of("111.444.777-35"));
        Veiculo veiculo = Veiculo.novo(Placa.of("ABC1D23"), "Fiat", "Uno", 2018, cliente);
        OrdemServico os = OrdemServico.receber(cliente, veiculo, "TRACK9999");
        os.iniciarDiagnostico();
        os.enviarOrcamento();
        os.recusarOrcamento();
        assertEquals(StatusOrdemServico.CANCELADA, os.getStatus());
    }
}
