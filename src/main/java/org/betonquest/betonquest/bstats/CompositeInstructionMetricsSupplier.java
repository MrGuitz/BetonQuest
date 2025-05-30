package org.betonquest.betonquest.bstats;

import org.betonquest.betonquest.id.ID;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A {@link InstructionMetricsSupplier} that is made up of {@link Supplier}s for each method. This allows creating from
 * lambda and method references.
 *
 * @param <T> kind of {@link ID} that is supplied
 */
public class CompositeInstructionMetricsSupplier<T extends ID> implements InstructionMetricsSupplier<T> {

    /**
     * The supplier to use for {@link ID}s.
     */
    private final Supplier<Set<T>> identifierSupplier;

    /**
     * The supplier to use for valid type strings.
     */
    private final Supplier<Set<String>> typeSupplier;

    /**
     * Create the composite supplier by providing the partial suppliers.
     *
     * @param identifierSupplier supplier for {@link ID}s
     * @param typeSupplier       supplier for types
     */
    public CompositeInstructionMetricsSupplier(final Supplier<Set<T>> identifierSupplier, final Supplier<Set<String>> typeSupplier) {
        this.identifierSupplier = identifierSupplier;
        this.typeSupplier = typeSupplier;
    }

    @Override
    public Set<T> getIdentifiers() {
        return identifierSupplier.get();
    }

    @Override
    public Set<String> getTypes() {
        return typeSupplier.get();
    }
}
