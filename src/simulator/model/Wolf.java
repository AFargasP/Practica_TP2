package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

    final static String WOLF_GENETIC_CODE = "Wolf";
    final static double INIT_SIGHT_WOLF = 50;
    final static double INIT_SPEED_WOLF = 60;
    final static double BOOST_FACTOR_WOLF = 3.0;
    final static double MAX_AGE_WOLF = 14.0;
    final static double FOOD_THRSHOLD_WOLF = 50.0;
    final static double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
    final static double FOOD_DROP_RATE_WOLF = 18.0;
    final static double FOOD_DROP_DESIRE_WOLF = 10.0;
    final static double FOOD_EAT_VALUE_WOLF = 50.0;
    final static double DESIRE_THRESHOLD_WOLF = 65.0;
    final static double DESIRE_INCREASE_RATE_WOLF = 30.0;
    final static double PREGNANT_PROBABILITY_WOLF = 0.75;

    private SelectionStrategy huntingStrategy;
    private Animal huntTarget;

    public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos) {
        super(WOLF_GENETIC_CODE, Diet.CARNIVORE, INIT_SIGHT_WOLF, INIT_SPEED_WOLF, mateStrategy, pos);
        this.huntingStrategy = huntingStrategy;
    }

    protected Wolf(Wolf p1, Animal p2) {
        super(p1, p2);
        this.huntingStrategy = p1.huntingStrategy;
        this.huntTarget = null;
    }

    private void selectHuntTarget() { //SELECCIONA HUNTTARGET POR ESTRATEGIA Y PREDICADO (ANIMALES HERVIVOROS)
        huntTarget = huntingStrategy.select(this,regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.HERBIVORE));
    }
    
    private void avanzaNormal(double dt) { // SI LLEGA A DESTINO ELIGE OTRO ALEATORIO, SE MUEVE, BAJA ENERGÍA Y SUBE DESEO
    	 if (pos.distanceTo(dest) < COLLISION_RANGE) {
             dest = Vector2D.getPosAleatoria(0.0, regionMngr.getWidth() - 1, 0.0, regionMngr.getHeight() - 1);
         }

         move(speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));

         energy -= FOOD_DROP_RATE_WOLF * dt;
         energy = Utils.constrainValueInRange(energy, 0.0, 100.0);

         desire += DESIRE_INCREASE_RATE_WOLF * dt;
         desire = Utils.constrainValueInRange(desire, 0.0, 100.0);
    }
    
    private void avanzaCazando(double dt) { //DEST = HUNTTARGET, SE MUEVE, RESTA ENERGIA Y SUMA DESEO. SI ESTA DENTRO DE COLLISION RANGE CAZA A HUNT TARGET Y SUMA ENERGIA
    	 dest = huntTarget.getPosition();
         move(BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));

         energy -= FOOD_DROP_RATE_WOLF * dt * FOOD_DROP_BOOST_FACTOR_WOLF;
         energy = Utils.constrainValueInRange(energy, 0.0, 100.0);

         desire += DESIRE_INCREASE_RATE_WOLF * dt;
         desire = Utils.constrainValueInRange(desire, 0.0, 100.0);

         if (pos.distanceTo(huntTarget.getPosition()) < COLLISION_RANGE) {
             huntTarget.setDeadStateAction();
             huntTarget = null;

             energy += FOOD_EAT_VALUE_WOLF;
             energy = Utils.constrainValueInRange(energy, 0.0, 100.0);
         }
    }
    
    private void avanzaPersiguiendoMateTarget(double dt) {  //DEST = MATETARGET, RESTA ENERGIA, SUMA DESEO. SI ESTA DENTRO DE COLLISION RANGE REVISA QUE NO ESTA EMBARAZADO Y APARECE UN LOBO EN FUNCIONDE ALGUNOS PARAMETROS
    	  dest = mateTarget.getPosition();
          move(BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));

          energy -= FOOD_DROP_RATE_WOLF * dt * FOOD_DROP_BOOST_FACTOR_WOLF;
          energy = Utils.constrainValueInRange(energy, 0.0, 100.0);

          desire += DESIRE_INCREASE_RATE_WOLF * dt;
          desire = Utils.constrainValueInRange(desire, 0.0, 100.0);

          if (pos.distanceTo(mateTarget.getPosition()) < COLLISION_RANGE) {
              mateTarget.setDesire(0.0);
              desire = 0.0;

              if (!isPregnant()) {
                  double x = Utils.RAND.nextDouble();
                  if (x < PREGNANT_PROBABILITY_WOLF) {
                      baby = new Wolf(this, mateTarget);
                  }
              }

              energy -= FOOD_DROP_DESIRE_WOLF;
              energy = Utils.constrainValueInRange(energy, 0.0, 100.0);

              mateTarget = null;
          }
    }

    private void normalState(double dt) { //AVANZA NORMAL Y CAMBIA DE ESTADO
    	avanzaNormal(dt);
        if (energy < FOOD_THRSHOLD_WOLF) {
            setHungerStateAction();
        } else if (desire > DESIRE_THRESHOLD_WOLF) {
            setMateStateAction();
        }
    }

    private void hungerState(double dt) {  // BUSCA PRESA SI NO TIENE O ESTÁ MUERTA/LEJOS. SI HAY PRESA LA CAZA, SI NO AVANZA NORMAL.CAMBIA DE ESTADO

        if (huntTarget == null || huntTarget.getState() == State.DEAD ||
            pos.distanceTo(huntTarget.getPosition()) > sightRange) {
            selectHuntTarget();
        }

        if (huntTarget == null) {
           avanzaNormal(dt);
        } else {
        	avanzaCazando(dt);
        }

        if (energy > 50) {
            if (desire < DESIRE_THRESHOLD_WOLF) {
                setNormalStateAction();
            } else {
                setMateStateAction();
            }
        }
    }

    private void mateState(double dt) {  // SI MATETARGET NO EXISTE O ESTÁ MUERTO/LEJOS, BUSCA OTRO. SI NO HAY, AVANZA NORMAL. SI HAY, LO PERSIGUE. CAMBIA DE ESTADO

        if (mateTarget != null && (mateTarget.getState() == State.DEAD ||
             pos.distanceTo(mateTarget.getPosition()) > sightRange)) {
            mateTarget = null;
        }

        if (mateTarget == null) {
            selectMateTarget();

            if (mateTarget == null) {
                avanzaNormal(dt);
            }

        } else {
        	avanzaPersiguiendoMateTarget(dt);
          
        }

        if (energy < FOOD_THRSHOLD_WOLF) {
            setHungerStateAction();
        } else if (desire < DESIRE_THRESHOLD_WOLF) {
            setNormalStateAction();
        }
    }

    public void update(double dt) {
        if (state == State.DEAD) return;

        switch (state) {
            case NORMAL:
                normalState(dt);
                break;
            case HUNGER:
                hungerState(dt);
                break;
            case MATE:
                mateState(dt);
                break;
        }

        age += dt; //SUMA DT A LA EDAD

        if (pos.isOut(regionMngr.getWidth() - 1, regionMngr.getHeight() - 1)) { // SI ESTA FUERA AJUSTA LA POSICION Y SE PONE A NORMAL STATE
            pos = pos.ajustaPosicion(regionMngr.getWidth() - 1, regionMngr.getHeight() - 1);
            setNormalStateAction();
        }

        if (energy <= 0.0 || age > MAX_AGE_WOLF) { //SE MUERE SI TIENE POCA ENERGIA O SI PASA SU EDAD MAX
            setDeadStateAction();
        }

        if (state != State.DEAD) { //LE DA COMIDA
            energy += regionMngr.getFood(this, dt);
            energy = Utils.constrainValueInRange(energy, 0.0, 100.0);
        }
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Diet getDiet() {
        return diet;
    }

    @Override
    protected void setNormalStateAction() {
        mateTarget = null;
        huntTarget = null;
        state = State.NORMAL;
    }

    @Override
    protected void setMateStateAction() {
        huntTarget = null;
        state = State.MATE;
    }

    @Override
    protected void setHungerStateAction() {
        mateTarget = null;
        state = State.HUNGER;
    }

    @Override
    protected void setDangerStateAction() {}

    @Override
    protected void setDeadStateAction() {
        mateTarget = null;
        huntTarget = null;
        state = State.DEAD;
    }
}
