package org.betonquest.betonquest.api.common.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.api.common.component.font.Font;
import org.betonquest.betonquest.api.common.component.font.FontRegistry;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The ComponentLineWrapper class is responsible for splitting a Component into multiple lines.
 */
@SuppressWarnings({"PMD.LooseCoupling", "PMD.TooManyMethods"})
public class ComponentLineWrapper {
    /**
     * The newline string to separate title and subtitle.
     */
    private static final Pattern NEW_LINE = Pattern.compile("\n");

    /**
     * The font registry used to get the width of the characters.
     */
    private final FontRegistry fontRegistry;

    /**
     * Creates a new ComponentLineWrapper instance.
     *
     * @param fontRegistry the font registry to use
     */
    public ComponentLineWrapper(final FontRegistry fontRegistry) {
        this.fontRegistry = fontRegistry;
    }

    /**
     * Splits a Component into multiple lines based on new line characters.
     *
     * @param component the Component to split
     * @return a list of Components, each representing a line
     */
    @SuppressWarnings("PMD.LooseCoupling")
    public static List<Component> splitNewLine(final Component component) {
        final LinkedList<Component> lines = splitNewLineTextComponent(component);
        if (component.children().isEmpty()) {
            return lines;
        }
        Component last = lines.removeLast();
        for (final Component child : component.children()) {
            final LinkedList<Component> childSegments = (LinkedList<Component>) splitNewLine(child);
            last = last.append(childSegments.removeFirst());
            for (final Component childSegment : childSegments) {
                lines.add(last.compact());
                last = Component.empty().style(last.style()).append(childSegment);
            }
        }
        lines.add(last.compact());
        return lines;
    }

    @SuppressWarnings("PMD.LooseCoupling")
    private static LinkedList<Component> splitNewLineTextComponent(final Component component) {
        if (!(component instanceof final TextComponent text)) {
            return new LinkedList<>(List.of(component));
        }
        return Arrays.stream(NEW_LINE.split(text.content(), -1))
                .map(segment -> Component.text(segment).style(text.style()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Wraps a Component into multiple lines based on the specified line width.
     *
     * @param component    the Component to wrap
     * @param maxLineWidth the maximum width of a line in pixels
     * @return a list of Components, each representing a line
     */
    public List<Component> splitWidth(final Component component, final int maxLineWidth) {
        return splitWidth(component, Component::empty, maxLineWidth);
    }

    /**
     * Wraps a Component into multiple lines based on the specified line width.
     *
     * @param component    the Component to wrap
     * @param linePrefix   a Supplier for the prefix of each line
     * @param maxLineWidth the maximum width of a line in pixels
     * @return a list of Components, each representing a line
     */
    public List<Component> splitWidth(final Component component, final Supplier<Component> linePrefix, final int maxLineWidth) {
        final List<Component> newLineWrapped = splitNewLine(component);

        final List<Component> resolvedLinePrefix = new ArrayList<>();
        final OffsetProvider offsetProvider = () -> {
            final Component prefix = linePrefix.get();
            resolvedLinePrefix.add(prefix);
            return width(prefix);
        };

        final List<Component> lines = newLineWrapped.stream().map(line -> wrap(line, new Offset(offsetProvider), maxLineWidth, new ComponentDecorations(line)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return appendPrefixes(resolvedLinePrefix, lines);
    }

    private List<Component> appendPrefixes(final List<Component> prefixes, final List<Component> lines) {
        if (prefixes.size() != lines.size()) {
            throw new IllegalStateException("Prefixes and lines must have the same size.");
        }
        final List<Component> result = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            result.add(prefixes.get(i).append(lines.get(i)).compact());
        }
        return result;
    }

    private LinkedList<Component> wrap(final Component component, final Offset offset, final int maxLineWidth, final ComponentDecorations decorations) {
        final LinkedList<Component> lines = wrapComponent(component, offset, maxLineWidth, decorations);
        if (component.children().isEmpty()) {
            return lines;
        }
        Component last = lines.removeLast();
        for (final Component child : component.children()) {
            final LinkedList<Component> childSegments = wrap(child, offset, maxLineWidth, decorations.getChild(child));
            last = last.append(childSegments.removeFirst());
            for (final Component childSegment : childSegments) {
                lines.add(last.compact());
                last = Component.empty().style(component.style()).append(childSegment);
            }
        }
        lines.add(last.compact());
        return lines;
    }

    private LinkedList<Component> wrapComponent(final Component component, final Offset offset, final int maxLineWidth, final ComponentDecorations decorations) {
        if (!(component instanceof final TextComponent text)) {
            return new LinkedList<>(List.of(component));
        }
        final String content = text.content();
        final Font font = fontRegistry.getFont(text.font());
        final List<String> segments = wrapText(font, content, offset, maxLineWidth, decorations);
        return segments.stream()
                .map(segment -> Component.text(segment).style(text.style()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @VisibleForTesting
    List<String> wrapText(final Font font, final String content, final Offset offset, final int maxLineWidth, final ComponentDecorations decorations) {
        final List<String> lines = new ArrayList<>();
        final StringBuilder currentLine = new StringBuilder();

        final String[] words = content.split(" ", -1);
        boolean firstWord = true;

        for (final String word : words) {
            final String wordWithSpace = firstWord ? word : " " + word;
            firstWord = false;
            final int wordWithSpaceWidth = getTextWidth(font, wordWithSpace, decorations);
            if (offset.getOffset() + wordWithSpaceWidth <= maxLineWidth) {
                currentLine.append(wordWithSpace);
                offset.addOffset(wordWithSpaceWidth);
                continue;
            }

            final int wordWidth = getTextWidth(font, word, decorations);
            if (wordWidth > maxLineWidth) {
                wrapWordExceedingLineLength(lines, currentLine, offset, font, wordWithSpace, maxLineWidth);
                continue;
            }

            lines.add(currentLine.toString());
            currentLine.setLength(0);
            currentLine.append(word);
            offset.reset();
            offset.addOffset(wordWidth);
        }

        if (!firstWord) {
            lines.add(currentLine.toString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }

        return lines;
    }

    private void wrapWordExceedingLineLength(final List<String> lines, final StringBuilder currentLine,
                                             final Offset offset, final Font font, final String word,
                                             final int maxLineWidth) {
        for (final char character : word.toCharArray()) {
            final int charWidth = font.getWidth(character);
            if (offset.getOffset() + charWidth > maxLineWidth) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                offset.reset();
            }
            if (currentLine.isEmpty() && character == ' ') {
                continue;
            }
            currentLine.append(character);
            offset.addOffset(charWidth);
        }
    }

    /**
     * Calculates the width of a Component in pixels.
     *
     * @param component the Component to calculate the width for
     * @return the width of the Component in pixels
     */
    public int width(final Component component) {
        return width(component, new ComponentDecorations(component));
    }

    private int width(final Component component, final ComponentDecorations decorations) {
        int width = 0;
        if (component instanceof final TextComponent text) {
            final Font font = fontRegistry.getFont(text.font());
            width = getTextWidth(font, text.content(), decorations);
        }
        return width + component.children().stream().mapToInt(child -> width(child, decorations.getChild(child))).sum();
    }

    private int getTextWidth(final Font font, final String text, final ComponentDecorations decorations) {
        int width = 0;
        final int decorationFix = getTextDecorationWidth(decorations);
        for (final char c : text.toCharArray()) {
            width += font.getWidth(c) + decorationFix;
        }
        return width;
    }

    private int getTextDecorationWidth(final ComponentDecorations decorations) {
        if (decorations.getDecorations().get(TextDecoration.BOLD) == TextDecoration.State.TRUE) {
            return 1;
        }
        return 0;
    }

    /**
     * The OffsetProvider interface is used to provide the offset in pixels for a new line.
     */
    @FunctionalInterface
    public interface OffsetProvider {
        /**
         * Gets the offset in pixels for a new line.
         *
         * @return the offset in pixels
         */
        int getOffset();
    }

    /**
     * The Offset class is used to store the offset of a line in pixels.
     */
    @VisibleForTesting
    static class Offset {
        /**
         * The offset to use even if {@link #reset()} is called.
         */
        private final OffsetProvider provider;

        /**
         * The offset of the line in pixels.
         */
        private int value;

        /**
         * Creates a new Offset instance with the default value of 0.
         */
        public Offset(final OffsetProvider provider) {
            this.provider = provider;
            this.value = provider.getOffset();
        }

        /**
         * Gets the current offset.
         *
         * @return the current offset
         */
        public int getOffset() {
            return value;
        }

        /**
         * Adds an offset to the current offset.
         *
         * @param offset the offset to add
         */
        public void addOffset(final int offset) {
            this.value += offset;
        }

        /**
         * Resets the offset to 0.
         */
        public void reset() {
            this.value = provider.getOffset();
        }
    }

    /**
     * The ComponentDecorations class is used to store the decorations of a Component.
     */
    @VisibleForTesting
    static class ComponentDecorations {
        /**
         * The map of TextDecoration to its state for the Component.
         */
        private final Map<TextDecoration, TextDecoration.State> decorations;

        /**
         * Creates a new ComponentDecorations instance for the given Component.
         *
         * @param component the Component to get the decorations for
         */
        public ComponentDecorations(final Component component) {
            this.decorations = component.decorations();
        }

        private ComponentDecorations(final Map<TextDecoration, TextDecoration.State> parent, final Component child) {
            this.decorations = parent;
            for (final Map.Entry<TextDecoration, TextDecoration.State> entry : child.decorations().entrySet()) {
                final TextDecoration decoration = entry.getKey();
                final TextDecoration.State state = entry.getValue();
                if (state != TextDecoration.State.NOT_SET) {
                    this.decorations.put(decoration, state);
                }
            }
        }

        /**
         * Creates a new ComponentDecorations instance for a child Component.
         *
         * @param component the child Component to get the decorations for
         * @return a new ComponentDecorations instance containing the decorations of the child Component
         */
        public ComponentDecorations getChild(final Component component) {
            return new ComponentDecorations(new EnumMap<>(decorations), component);
        }

        /**
         * Gets the decorations of the Component.
         *
         * @return a map of TextDecoration to its state
         */
        public Map<TextDecoration, TextDecoration.State> getDecorations() {
            return decorations;
        }
    }
}
