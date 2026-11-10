package cn.sparrowmini.security.api;
import java.util.Objects;
public record Subject(Kind kind, String id) {
 public enum Kind { USER, ROLE, GROUP }
 public Subject { Objects.requireNonNull(kind); if(Objects.requireNonNull(id).isBlank()) throw new IllegalArgumentException("Blank subject ID"); }
 public boolean matches(ActorContext actor) { return switch(kind) { case USER -> id.equals(actor.subject()); case ROLE -> actor.roles().contains(id); case GROUP -> actor.groups().contains(id); }; }
}
