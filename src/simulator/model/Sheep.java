package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

    final static String SHEEP_GENETIC_CODE = "Sheep";
    final static double INIT_SIGHT_SHEEP = 40;
    final static double INIT_SPEED_SHEEP = 35;
    final static double BOOST_FACTOR_SHEEP = 2.0;
    final static double MAX_AGE_SHEEP = 8;
    final static double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
    final static double FOOD_DROP_RATE_SHEEP = 20.0;
    final static double DESIRE_THRESHOLD_SHEEP = 65.0;
    final static double DESIRE_INCREASE_RATE_SHEEP = 40.0;
    final static double PREGNANT_PROBABILITY_SHEEP = 0.9;

    private SelectionStrategy dangerStrategy;
    private Animal dangerSource;

    protected Sheep(Sheep p1, Animal p2) {
        super(p1, p2);
        this.dangerStrategy = p1.dangerStrategy;
        this.dangerSource = null;
    }

    public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos) {
        super(SHEEP_GENETIC_CODE, Diet.HERBIVORE, INIT_SIGHT_SHEEP, INIT_SPEED_SHEEP, mateStrategy, pos);
        this.dangerStrategy = dangerStrategy;
    }

    private void selectDangerTarget() { //ELIGE EN FUNCION DE LA ESTRATEGIA Y EL PREDICADO QUE LE PASEMOS (ANIMALES CARNIVOROS)
        dangerSource = dangerStrategy.select(this, regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE)); 
    }


    private void avanzaNormal(double dt) { //BUSCA DESTINO SI ESTA A MENOS DE COLLISION RANGE, SE MUEVE, RESTA ENERGIA Y SUMA DESEO
        if (pos.distanceTo(dest) < COLLISION_RANGE) {
            dest = Vector2D.getPosAleatoria(0.0, regionMngr.getWidth() - 1, 0.0, regionMngr.getHeight() - 1);
        }

        move(speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));

        energy -= FOOD_DROP_RATE_SHEEP * dt;
        energy = Utils.constrainValueInRange(energy, 0.0, 100.0);

        desire += DESIRE_INCREASE_RATE_SHEEP * dt;
        desire = Utils.constrainValueInRange(desire, 0.0, 100.0);
    }

    private void avanzaHuyendo(double dt) { //DEST EN DIRECCCION CONTRARIA AL PELIGRO, SE MUEVE, RESTA ENERGIA Y SUMA DESEO
        dest = pos.plus(pos.minus(dangerSource.getPosition().direction()));

        move(BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));

        energy -= FOOD_DROP_RATE_SHEEP * FOOD_DROP_BOOST_FACTOR_SHEEP * dt;
        energy = Utils.constrainValueInRange(energy, 0.0, 100.0);

        desire += DESIRE_INCREASE_RATE_SHEEP * dt;
        desire = Utils.constrainValueInRange(desire, 0.0, 100.0);
    }

    private void avanzaPersiguiendoMateTarget(double dt) { //DEST ES MATETARGET, SE MUEVE, RESTA ENERGIA Y SUMA DESEO. COMPRUEBA DIST Y APARECE NUEVO BEBE DEPENDIENDO LAS CONDICIONES
        dest = mateTarget.getPosition();

        move(BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));

        energy -= FOOD_DROP_RATE_SHEEP * FOOD_DROP_BOOST_FACTOR_SHEEP * dt;
        energy = Utils.constrainValueInRange(energy, 0.0, 100.0);

        desire += DESIRE_INCREASE_RATE_SHEEP * dt;
        desire = Utils.constrainValueInRange(desire, 0.0, 100.0);

        if (pos.distanceTo(mateTarget.getPosition()) < COLLISION_RANGE) {
            desire = 0.0;
            mateTarget.setDesire(0.0);

            if (!isPregnant()) {
                double x = Utils.RAND.nextDouble();
                if (x < PREGNANT_PROBABILITY_SHEEP) {
                    baby = new Sheep(this, mateTarget);
                }
            }

            mateTarget = null;
        }
    }

    private void normalState(double dt) { //AVANZA NORMAL, ELIGE DANGERTARGET Y CAMBIA DE ESTADO
        avanzaNormal(dt);

        if (dangerSource == null) {
            selectDangerTarget();
        }

        if (dangerSource != null) {
            setDangerStateAction();
        } else if (desire > DESIRE_THRESHOLD_SHEEP) {
            setMateStateAction();
        }
    }

    private void dangerState(double dt) { //DANGERSOURCE == NULL, SI EXISTE Y ESTA MUERTO, HUYE SI EXISTE, SINO AVANZA NORMAL. BUSCA DANGER Y CAMBIA DE ESTADO
        if (dangerSource != null && dangerSource.getState() == State.DEAD) {
            dangerSource = null;
        }

        if (dangerSource != null) {
            avanzaHuyendo(dt);
        } else {
            avanzaNormal(dt);
        }

        if (dangerSource == null || pos.distanceTo(dangerSource.getPosition()) > sightRange) {
            selectDangerTarget();
        }

        if (dangerSource == null) {
            if (desire < DESIRE_THRESHOLD_SHEEP) {
                setNormalStateAction();
            } else {
                setMateStateAction();
            }
        }
    }

    private void mateState(double dt) { //BUSCA MATE TARGET EN CASO DE Q NO EXISTA O ESTE MUERTO O LEJOS. SI EXISTE LA PERSIGUE, SINO AVANZA NORMAL. POR ULTIMO CAMBIA DE ESTADO
        if (mateTarget == null ||
            mateTarget.getState() == State.DEAD ||
            pos.distanceTo(mateTarget.getPosition()) > sightRange) {

            selectMateTarget();
        }

        if (mateTarget == null) {
            avanzaNormal(dt);
        } else {
            avanzaPersiguiendoMateTarget(dt);
        }

        if (dangerSource == null) {
            selectDangerTarget();
            if (dangerSource != null) {
                setDangerStateAction();
            } else if (desire < DESIRE_THRESHOLD_SHEEP) {
                setNormalStateAction();
            }
        }
    }


    public void update(double dt) {
        if (state == State.DEAD) return;

        switch (state) {
            case NORMAL:
                normalState(dt);
                break;
            case DANGER:
                dangerState(dt);
                break;
            case MATE:
                mateState(dt);
                break;
        }

        age += dt; //SUMA DT A LA EDAD

        if (pos.isOut(regionMngr.getWidth() - 1, regionMngr.getHeight() - 1)) { //SI ESTA FUERA AJUSTA Y PONE A NORMAL STATE
            pos = pos.ajustaPosicion(regionMngr.getWidth() - 1, regionMngr.getHeight() - 1);
            setNormalStateAction();
        }

        if (age > MAX_AGE_SHEEP || energy <= 0.0) { //SI ES MAYOR O NO TIENE ENERGIA MUERE
            setDeadStateAction();
        }

        if (state != State.DEAD) { //LE DA COMIDA LA REGION EN LA QUE ESTA
            energy += regionMngr.getFood(this, dt);
            energy = Utils.constrainValueInRange(energy, 0.0, 100.0);
        }
    }


    @Override
    public Diet getDiet() {
        return diet;
    }

    @Override
    protected void setNormalStateAction() {
        mateTarget = null;
        dangerSource = null;
        state = State.NORMAL;
    }

    @Override
    protected void setMateStateAction() {
        dangerSource = null;
        state = State.MATE;
    }

    @Override
    protected void setHungerStateAction() {}

    @Override
    protected void setDangerStateAction() {
        mateTarget = null;
        state = State.DANGER;
    }

    @Override
    protected void setDeadStateAction() {
        mateTarget = null;
        dangerSource = null;
        state = State.DEAD;
    }

    @Override
    public State getState() {
        return state;
    }
}
