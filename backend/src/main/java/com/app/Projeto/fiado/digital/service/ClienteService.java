package com.app.Projeto.fiado.digital.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.dto.ClienteDTO;
import com.app.Projeto.fiado.digital.dto.ClienteResumoDTO;
import com.app.Projeto.fiado.digital.exception.BusinessException;
import com.app.Projeto.fiado.digital.exception.ResourceNotFoundException;
import com.app.Projeto.fiado.digital.model.Cliente;
import com.app.Projeto.fiado.digital.model.Comercio;
import com.app.Projeto.fiado.digital.model.Venda;
import com.app.Projeto.fiado.digital.repository.ClienteRepository;
import com.app.Projeto.fiado.digital.repository.PagamentoRepository;
import com.app.Projeto.fiado.digital.repository.VendaRepository;
import com.app.Projeto.fiado.digital.security.ComercioContextService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ComercioContextService comercioContext;

    @Transactional
    public ClienteDTO salvar(ClienteDTO dto) {
        Comercio comercio = comercioContext.getComercioLogado();

        if (dto.getCpf() != null && !dto.getCpf().isBlank()) {
            // Remove pontuação para garantir a busca limpa por números
            String cpfLimpo = dto.getCpf().replaceAll("\\D", "");
            
            // CORREÇÃO: Passou a validar se o CPF existe apenas DENTRO deste comércio
            boolean existeCpfNoComercio = clienteRepository.existsByCpfAndComercioId(cpfLimpo, comercio.getId());
            if (existeCpfNoComercio) {
                throw new BusinessException("Já existe um cliente cadastrado com o CPF informado no seu estabelecimento.");
            }
            dto.setCpf(cpfLimpo); // Garante que salva apenas os números
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome().trim());
        cliente.setCpf(dto.getCpf());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEndereco(dto.getEndereco());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setComercio(comercio);

        cliente = clienteRepository.save(cliente);
        return converterParaDTO(cliente);
    }

    public List<ClienteDTO> listarTodos() {
        Long comercioId = comercioContext.getComercioIdLogado();
        return clienteRepository.findByComercioId(comercioId).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<ClienteResumoDTO> listarDevedores() {
        Long comercioId = comercioContext.getComercioIdLogado();
        return clienteRepository.findClientesComSaldoDevedorByComercioId(comercioId).stream()
                .map(proj -> new ClienteResumoDTO(
                        proj.getClienteId(),
                        proj.getNome(),
                        proj.getCpf(),
                        BigDecimal.valueOf(proj.getTotalDevido()),
                        proj.getTelefone()))
                .collect(Collectors.toList());
    }

    public ClienteDTO buscarPorId(Long id) {
        Cliente cliente = buscarClienteDoComercio(id);
        return converterParaDTO(cliente);
    }

    public List<ClienteDTO> buscarPorNome(String nome) {
        Long comercioId = comercioContext.getComercioIdLogado();
        return clienteRepository.findByComercioIdAndNomeContainingIgnoreCase(comercioId, nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO atualizar(Long id, ClienteDTO dto) {
        Cliente cliente = buscarClienteDoComercio(id);
        Long comercioId = comercioContext.getComercioIdLogado();

        if (dto.getCpf() != null && !dto.getCpf().isBlank()) {
            String cpfLimpo = dto.getCpf().replaceAll("\\D", "");
            
            // CORREÇÃO NA ATUALIZAÇÃO: Busca o cliente pelo CPF dentro do comércio logado
            clienteRepository.findByCpfAndComercioId(cpfLimpo, comercioId).ifPresent(existente -> {
                // Se encontrar o CPF, mas pertencer a OUTRO cliente do mesmo comércio, aí sim barra
                if (!existente.getId().equals(id)) {
                    throw new BusinessException("Já existe um cliente cadastrado com o CPF informado no seu estabelecimento.");
                }
            });
            dto.setCpf(cpfLimpo);
        }

        cliente.setNome(dto.getNome().trim());
        cliente.setCpf(dto.getCpf());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEndereco(dto.getEndereco());
        cliente.setDataNascimento(dto.getDataNascimento());

        cliente = clienteRepository.save(cliente);
        return converterParaDTO(cliente);
    }

    @Transactional
    public void deletar(Long id) {
        buscarClienteDoComercio(id);
        clienteRepository.deleteById(id);
    }

    public BigDecimal calcularTotalDevido(Long clienteId) {
    Long comercioId = comercioContext.getComercioIdLogado();
    List<Venda> vendasEmAberto = vendaRepository.findVendasEmAbertoPorClienteAndComercioId(clienteId, comercioId);

    return vendasEmAberto.stream()
            .map(venda -> {
                BigDecimal pago = pagamentoRepository.somarPagamentosDaVenda(venda.getId());
                // Se o retorno de somarPagamentosDaVenda puder ser nulo, trate aqui com um Optional ou verificação
                BigDecimal totalPago = (pago != null) ? pago : BigDecimal.ZERO;
                return venda.getValorTotal().subtract(totalPago);
            })
            
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

    private Cliente buscarClienteDoComercio(Long id) {
        Long comercioId = comercioContext.getComercioIdLogado();
        return clienteRepository.findByIdAndComercioId(id, comercioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    private ClienteDTO converterParaDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setCpf(cliente.getCpf());
        dto.setTelefone(cliente.getTelefone());
        dto.setEndereco(cliente.getEndereco());
        dto.setDataNascimento(cliente.getDataNascimento());
        dto.setTotalDevido(calcularTotalDevido(cliente.getId()));
        return dto;
    }
}