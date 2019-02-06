package com.vencimento.contract

import com.vencimento.state.StatusVencimento
import com.vencimento.state.VencimentoState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class VencimentoContract : Contract {

    override fun verify(tx: LedgerTransaction) {
        val comando = tx.commandsOfType<Commands>().single()

        when (comando.value) {
            is Commands.Emitir -> verifyEmitir(tx)
            is Commands.Aceitar -> verifyResposta(tx, StatusVencimento.APagar, StatusVencimento.Emitido)
            is Commands.Recusar -> verifyResposta(tx, StatusVencimento.Recusado, StatusVencimento.Emitido)
            is Commands.Pagar -> verifyResposta(tx, StatusVencimento.Pago, StatusVencimento.APagar)
        }

        requireThat {
            // regras validas para todos os comandos
            "Todos os participantes devem assinar a transação." using
                    tx.outputs.all {
                        val todosOsAssinantes = tx.commands.flatMap { it.signers }
                        val chavesDosParticipantes = it.data.participants
                                .map { it.owningKey }

                        todosOsAssinantes.containsAll(chavesDosParticipantes)
                    }
        }
    }

    private fun verifyResposta(tx: LedgerTransaction,
                               statusNovo: StatusVencimento,
                               statusAtual: StatusVencimento) {
        requireThat {
            //validações da transação
            "Deve haver input." using (tx.inputsOfType<VencimentoState>().isNotEmpty())
            "Deve haver output." using (tx.outputsOfType<VencimentoState>().isNotEmpty())

            val inputs = tx.inputsOfType<VencimentoState>().sortedBy { it.linearId }
            val outputs = tx.outputsOfType<VencimentoState>().sortedBy { it.linearId }

            "As listas de input e output devem ter o mesmo tamanho." using (
                    inputs.size == outputs.size)

            val inputsEOutputs = inputs.zip(outputs)

            "Os inputs e outputs devem possuir os mesmos ids." using (
                    inputsEOutputs.all { it.first.linearId == it.second.linearId })

            //validações do negócio
            "Os valores do input e output devem ser os mesmo." using (
                    inputsEOutputs.all { it.first.valor == it.second.valor })
            "O De do input e output devem ser os mesmo." using (
                    inputsEOutputs.all { it.first.de == it.second.de })
            "O Para do input e output devem ser os mesmo." using (
                    inputsEOutputs.all { it.first.para == it.second.para })
            "O Status do output deve ser $statusNovo." using (outputs.all {
                it.statusVencimento == statusNovo } )
            "O Status do input deve ser $statusAtual." using (inputs.all {
                it.statusVencimento == statusAtual} )
        }
    }

    private fun verifyEmitir(tx: LedgerTransaction) {
        requireThat {
            //validações da transação
            "Não pode haver input." using tx.inputsOfType<VencimentoState>().isEmpty()
            "Deve haver um output." using (tx.outputsOfType<VencimentoState>().size == 1)

            val outputs = tx.outputsOfType<VencimentoState>()

            //validações do negócio
            "O De e o Para não podem ser iguais." using (outputs.all { it.de != it.para })
            "O valor deve ser maior que zero." using (outputs.all { it.valor > 0 })
            "O Status do Vencimento deve ser ${StatusVencimento.Emitido}." using (
                    outputs.all { it.statusVencimento == StatusVencimento.Emitido })
        }
    }

    private fun verifyPagar(tx: LedgerTransaction) {

    }

    private fun verifyRecusar(tx: LedgerTransaction) {
    }

    interface Commands : CommandData {
        class Emitir : Commands
        class Aceitar : Commands
        class Recusar : Commands
        class Pagar : Commands
    }

}