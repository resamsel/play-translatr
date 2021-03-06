package assertions;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.AbstractAssert;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by resamsel on 24/07/2017.
 */
public abstract class AbstractGenericAssert<S extends AbstractGenericAssert<S, A>, A> extends
        AbstractAssert<S, A> {

  protected final String name;

  protected AbstractGenericAssert(String name, A actual, Class<S> selfType) {
    super(actual, selfType);
    this.name = name;
  }

  protected S isNull(String field, Object actual) {
    assertThat(actual)
            .overridingErrorMessage("Expected %s's %s to be null, but was <%s> (%s)", name, field,
                    actual, descriptionText())
            .isNull();
    return myself;
  }

  S isTrue(String field, Boolean actual) {
    assertThat(actual)
            .overridingErrorMessage("Expected %s's %s to be true, but was <%s> (%s)", name, field,
                    actual, descriptionText())
            .isTrue();
    return myself;
  }

  S isFalse(String field, Boolean actual) {
    assertThat(actual)
            .overridingErrorMessage("Expected %s's %s to be false, but was <%s> (%s)", name, field,
                    actual, descriptionText())
            .isFalse();
    return myself;
  }

  protected <T> S isEqualTo(T expected, T actual, String newErrorMessage, Object... args) {
    assertThat(actual)
            .overridingErrorMessage(newErrorMessage, args)
            .isEqualTo(expected);
    return myself;
  }

  protected S isEqualTo(String field, int expected, int actual) {
    return isEqualTo(
            expected, actual,
            "Expected %s's %s to be <%s> but was <%s> (%s)",
            name, field, expected, actual, descriptionText()
    );
  }

  protected <T> S isEqualTo(String field, T expected, T actual) {
    return isEqualTo(
            expected, actual,
            "Expected %s's %s to be <%s> but was <%s> (%s)",
            name, field, expected, actual, descriptionText()
    );
  }

  protected S isEqualTo(String field, String expected, String actual) {
    return isEqualTo(
            expected, actual,
            "Expected %s's %s to be <%s> but was <%s> (%s)",
            name, field, expected, StringUtils.abbreviate(actual.trim(), 50), descriptionText()
    );
  }

  protected S contains(String field, String s, String actual) {
    assertThat(actual)
            .overridingErrorMessage("Expected %s's %s to contain <%s> but was <%s> (%s)", name, field,
                    s, StringUtils.abbreviate(actual.trim(), 50), descriptionText())
            .contains(s);
    return myself;
  }

  S hasSize(String field, int expected, Collection<?> actual) {
    assertThat(actual)
            .overridingErrorMessage("Expected %s's %s to have a size of <%d> but was <%d> (%s)", name,
                    field, expected, actual.size(), descriptionText())
            .hasSize(expected);
    return myself;
  }

  S hasSize(String field, int expected, Map<?, ?> actual) {
    assertThat(actual)
            .overridingErrorMessage("Expected %s's %s to have a size of <%d> but was <%d> (%s)", name,
                    field, expected, actual != null ? actual.size() : 0, descriptionText())
            .hasSize(expected);
    return myself;
  }

  protected <T> S contains(String field, T expected, Collection<T> actual) {
    assertThat(actual)
            .overridingErrorMessage(
                    "Expected %s's %s to contain <%s> but it did not <%s> (%s)",
                    name,
                    field,
                    expected,
                    actual,
                    descriptionText())
            .contains(expected);
    return myself;
  }

  @SafeVarargs
  protected final <T, U> S contains(String field, Map<T, U> actual, Map.Entry<T, U>... expected) {
    assertThat(actual)
            .overridingErrorMessage(
                    "Expected %s's %s to contain <%s> but it did not <%s> (%s)",
                    name,
                    field,
                    expected,
                    actual,
                    descriptionText())
            .contains(expected);
    return myself;
  }

  protected <T, U> S containsKey(String field, T expected, Map<T, U> actual) {
    assertThat(actual)
            .overridingErrorMessage(
                    "Expected %s's %s to contain <%s> but it did not <%s> (%s)",
                    name,
                    field,
                    expected,
                    actual,
                    descriptionText())
            .containsKey(expected);
    return myself;
  }
}
