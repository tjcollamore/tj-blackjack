import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class ChipStackView extends VBox
{
  private static final double CHIP_SIZE = 42;
  private static final double STACK_OFFSET_Y = 4;

  private static final int MAX_CHIPS_PER_PILE = 8;

  /*
   * Comfortable chip inventories during play.
   *
   * If a denomination grows beyond these amounts,
   * some of the excess is colored up.
   */
  private static final int MAX_GREEN_CHIPS = 24;
  private static final int MAX_BLACK_CHIPS = 8;
  private static final int MAX_PURPLE_CHIPS = 4;

  /*
   * Bankroll pile layout.
   *
   * Two piles per row keeps the chip area compact.
   * ROW_STEP being less than a full stack height
   * creates the overlapping/depth effect.
   */
  private static final int PILES_PER_ROW = 2;

  private static final double BANKROLL_AREA_WIDTH = 140;
  private static final double COLUMN_STEP = 52;
  private static final double ROW_STEP = 45;

  private Pane chipArea =
      new Pane();


  public ChipStackView()
  {
    setAlignment(
        Pos.BOTTOM_CENTER
    );

    chipArea.setMinWidth(
        BANKROLL_AREA_WIDTH
    );

    chipArea.setPrefWidth(
        BANKROLL_AREA_WIDTH
    );

    getChildren().add(
        chipArea
    );
  }


  public void setAmount(
      double amount,
      ChipDisplayMode mode)
  {
    chipArea
        .getChildren()
        .clear();

    int cents =
        (int) Math.round(
            amount * 100
        );

      if (mode == ChipDisplayMode.BANKROLL)
      {
        layoutBankrollPiles(
            getBankrollComposition(cents)
        );
      }
      else if (mode == ChipDisplayMode.WAGER)
      {
        layoutWagerPiles(
            getColorUpComposition(cents)
        );
      }
      else
      {
        layoutColorUpPiles(
            getColorUpComposition(cents)
        );
      }
    }

    private void layoutWagerPiles(
        List<ChipPile> piles)
    {
      if (piles.isEmpty())
      {
        setChipAreaSize(
            CHIP_SIZE,
            CHIP_SIZE
        );

        return;
      }

      final double columnStep = 34;
      final double rowStep = 30;

      final int pilesPerRow = 3;

      int rows =
          (piles.size()
           + pilesPerRow - 1)
          / pilesPerRow;

      int pileIndex = 0;

      for (int row = 0;
           row < rows;
           row++)
      {
        int remaining =
            piles.size()
            - pileIndex;

        int pilesInRow =
            Math.min(
                pilesPerRow,
                remaining
            );

        double rowWidth =
            CHIP_SIZE
            + (pilesInRow - 1)
              * columnStep;

        double startX =
            (105 - rowWidth)
            / 2.0;

        for (int column = 0;
             column < pilesInRow;
             column++)
        {
          ChipPile pile =
              piles.get(
                  pileIndex
              );

          Pane stack =
              createChipStack(
                  pile.getChip(),
                  pile.getCount()
              );

          applyNaturalOffset(
              stack,
              pileIndex
          );

          stack.setLayoutX(
              startX
              + column
                * columnStep
          );

          stack.setLayoutY(
              row
              * rowStep
          );

          chipArea
              .getChildren()
              .add(
                  stack
              );

          pileIndex++;
        }
      }

      double maxStackHeight =
          CHIP_SIZE
          + (MAX_CHIPS_PER_PILE - 1)
            * STACK_OFFSET_Y;

      double totalHeight =
          (rows - 1)
          * rowStep
          + maxStackHeight
          + 4;

      setChipAreaSize(
          105,
          totalHeight
      );
    }
    
  /*
   * Creates a believable working bankroll.
   *
   * We intentionally keep useful lower
   * denominations instead of always minimizing
   * the number of chips.
   *
   * As stacks become excessive, some chips are
   * gradually promoted into larger denominations.
   */
  private List<ChipPile> getBankrollComposition(
      int cents)
  {
    List<ChipPile> piles =
        new ArrayList<>();

    int originalCents =
        cents;


    /*
     * Start by expressing the bankroll primarily
     * as normal working chips.
     */
    int greenCount =
        cents
        / WagerChip.GREEN.getCentValue();

    cents %=
        WagerChip.GREEN.getCentValue();


    int redCount =
        cents
        / WagerChip.RED.getCentValue();

    cents %=
        WagerChip.RED.getCentValue();


    int pinkCount =
        cents
        / WagerChip.PINK.getCentValue();

    cents %=
        WagerChip.PINK.getCentValue();


    int whiteCount =
        cents
        / WagerChip.WHITE.getCentValue();


    int blackCount = 0;
    int purpleCount = 0;
    int orangeCount = 0;


    /*
     * Too many greens:
     *
     * 4 × $25 = 1 × $100
     */
    if (greenCount > MAX_GREEN_CHIPS)
    {
      int excess =
          greenCount
          - MAX_GREEN_CHIPS;

      int greensToPromote =
          ((excess + 3) / 4) * 4;

      greenCount -=
          greensToPromote;

      blackCount +=
          greensToPromote / 4;
    }


    /*
     * Too many blacks:
     *
     * 5 × $100 = 1 × $500
     */
    if (blackCount > MAX_BLACK_CHIPS)
    {
      int excess =
          blackCount
          - MAX_BLACK_CHIPS;

      int blacksToPromote =
          ((excess + 4) / 5) * 5;

      blackCount -=
          blacksToPromote;

      purpleCount +=
          blacksToPromote / 5;
    }


    /*
     * Too many purples:
     *
     * 2 × $500 = 1 × $1,000
     */
    if (purpleCount > MAX_PURPLE_CHIPS)
    {
      int excess =
          purpleCount
          - MAX_PURPLE_CHIPS;

      int purplesToPromote =
          ((excess + 1) / 2) * 2;

      purpleCount -=
          purplesToPromote;

      orangeCount +=
          purplesToPromote / 2;
    }


    /*
     * Keep some $5 chips around during normal
     * table play.
     *
     * Otherwise an exact denomination breakdown
     * can often eliminate reds entirely.
     */
    if (originalCents >= 10000
        && redCount < 5
        && greenCount > 0)
    {
      greenCount--;
      redCount += 5;
    }


    /*
     * Larger denominations first.
     *
     * This also means they tend to appear toward
     * the back/top of the rendered bankroll pile.
     */
    addPiles(
        piles,
        WagerChip.ORANGE,
        orangeCount
    );

    addPiles(
        piles,
        WagerChip.PURPLE,
        purpleCount
    );

    addPiles(
        piles,
        WagerChip.BLACK,
        blackCount
    );

    addPiles(
        piles,
        WagerChip.GREEN,
        greenCount
    );

    addPiles(
        piles,
        WagerChip.RED,
        redCount
    );

    addPiles(
        piles,
        WagerChip.PINK,
        pinkCount
    );

    addPiles(
        piles,
        WagerChip.WHITE,
        whiteCount
    );

    return piles;
  }


  /*
   * Color Up intentionally does the opposite.
   *
   * The player is leaving, so use the largest
   * possible denominations.
   */
  private List<ChipPile> getColorUpComposition(
      int cents)
  {
    List<ChipPile> piles =
        new ArrayList<>();

    for (WagerChip chip :
         List.of(
             WagerChip.ORANGE,
             WagerChip.PURPLE,
             WagerChip.BLACK,
             WagerChip.GREEN,
             WagerChip.RED,
             WagerChip.PINK,
             WagerChip.WHITE
         ))
    {
      int count =
          cents
          / chip.getCentValue();

      cents %=
          chip.getCentValue();

      addPiles(
          piles,
          chip,
          count
      );
    }

    return piles;
  }


  /*
   * Splits large quantities into physical piles.
   *
   * Example:
   *
   * 21 greens
   *
   * becomes:
   *
   * 8
   * 8
   * 5
   */
  private void addPiles(
      List<ChipPile> piles,
      WagerChip chip,
      int count)
  {
    while (count > 0)
    {
      int pileCount =
          Math.min(
              count,
              MAX_CHIPS_PER_PILE
          );

      piles.add(
          new ChipPile(
              chip,
              pileCount
          )
      );

      count -=
          pileCount;
    }
  }


  /*
   * Bankroll layout:
   *
   * Staggered rows with deliberate overlap.
   *
   * Back rows are added first and front rows
   * afterward, so lower piles visually sit
   * in front of upper piles.
   */
  private void layoutBankrollPiles(
      List<ChipPile> piles)
  {
    double actualBottom = 0;
    if (piles.isEmpty())
    {
      setChipAreaSize(
          BANKROLL_AREA_WIDTH,
          CHIP_SIZE
      );

      return;
    }

    int numberOfRows =
        (piles.size()
         + PILES_PER_ROW
         - 1)
        / PILES_PER_ROW;


    double[] rowNudges =
        {
          4,
          -3,
          5,
          -5,
          2,
          -2
        };


    int pileIndex = 0;

    for (int row = 0;
         row < numberOfRows;
         row++)
    {
      int pilesRemaining =
          piles.size()
          - pileIndex;

      int pilesInRow =
          Math.min(
              PILES_PER_ROW,
              pilesRemaining
          );


      double rowWidth =
          CHIP_SIZE
          + (pilesInRow - 1)
            * COLUMN_STEP;


      double startX =
          (BANKROLL_AREA_WIDTH
           - rowWidth)
          / 2.0;


      startX +=
          rowNudges[
              row
              % rowNudges.length
          ];


      for (int column = 0;
           column < pilesInRow;
           column++)
      {
        ChipPile pile =
            piles.get(
                pileIndex
            );


        Pane stack =
            createChipStack(
                pile.getChip(),
                pile.getCount()
            );


        applyNaturalOffset(
            stack,
            pileIndex
        );


        stack.setLayoutX(
            startX
            + column
              * COLUMN_STEP
        );


        stack.setLayoutY(
            row
            * ROW_STEP
        );

          double stackBottom =
            stack.getLayoutY()
            + stack.getPrefHeight();
    
          actualBottom =
            Math.max(
                actualBottom,
                stackBottom
            );
    

        /*
         * Because children added later render
         * over children added earlier, lower
         * rows naturally appear in front.
         */
        chipArea
            .getChildren()
            .add(
                stack
            );


        pileIndex++;
      }
    }

    /*
     * Leave enough room for the final/front row
     * plus its tallest possible stack.
     */
    double totalHeight = actualBottom + 2;
    setChipAreaSize(BANKROLL_AREA_WIDTH, totalHeight);
  }


  /*
   * Color Up stays compact and mostly horizontal.
   *
   * The comparison dialog should show the newly
   * compressed chips clearly rather than making
   * them look like another messy playing bankroll.
   */
  private void layoutColorUpPiles(
      List<ChipPile> piles)
  {
    if (piles.isEmpty())
    {
      setChipAreaSize(
          CHIP_SIZE,
          CHIP_SIZE
      );

      return;
    }


    final double colorUpColumnStep =
        48;


    double totalWidth =
        CHIP_SIZE
        + (piles.size() - 1)
          * colorUpColumnStep;


    double maxStackHeight =
        CHIP_SIZE
        + (MAX_CHIPS_PER_PILE - 1)
          * STACK_OFFSET_Y;


    for (int i = 0;
         i < piles.size();
         i++)
    {
      ChipPile pile =
          piles.get(i);


      Pane stack =
          createChipStack(
              pile.getChip(),
              pile.getCount()
          );


      applyNaturalOffset(
          stack,
          i
      );


      stack.setLayoutX(
          i
          * colorUpColumnStep
      );


      /*
       * Small alternating offset keeps even
       * the color-up pile from being perfectly
       * mechanical.
       */
      stack.setLayoutY(
          i % 2 == 0
              ? 2
              : 0
      );


      chipArea
          .getChildren()
          .add(
              stack
          );
    }


    setChipAreaSize(
        totalWidth,
        maxStackHeight + 6
    );
  }


  private void setChipAreaSize(
      double width,
      double height)
  {
    chipArea.setMinSize(
        width,
        height
    );

    chipArea.setPrefSize(
        width,
        height
    );

    chipArea.setMaxSize(
        width,
        height
    );
  }


  /*
   * Creates one physical stack.
   *
   * Small deterministic imperfections are applied
   * to the individual chips so each pile looks
   * hand-stacked rather than computer-perfect.
   */
  private Pane createChipStack(
      WagerChip chip,
      int count)
  {
    double stackHeight =
        CHIP_SIZE
        + (count - 1)
          * STACK_OFFSET_Y;


    Pane stack =
        new Pane();


    /*
     * Extra horizontal room allows the individual
     * chips to wobble slightly without clipping.
     */
    stack.setMinSize(
        CHIP_SIZE + 8,
        stackHeight + 4
    );

    stack.setPrefSize(
        CHIP_SIZE + 8,
        stackHeight + 4
    );

    stack.setMaxSize(
        CHIP_SIZE + 8,
        stackHeight + 4
    );


    Image image =
        loadChipImage(
            chip
        );


    DropShadow shadow =
        new DropShadow(
            4,
            Color.rgb(
                0,
                0,
                0,
                0.45
            )
        );


    double baseShift =
        getStackBaseShift(
            chip,
            count
        );


    for (int i = 0;
         i < count;
         i++)
    {
      ImageView chipView =
          new ImageView(
              image
          );


      chipView.setFitWidth(
          CHIP_SIZE
      );

      chipView.setFitHeight(
          CHIP_SIZE
      );

      chipView.setPreserveRatio(
          true
      );


      double xJitter =
          getChipXJitter(
              chip,
              count,
              i
          );


      double yJitter =
          getChipYJitter(
              chip,
              count,
              i
          );


      double rotation =
          getChipRotation(
              chip,
              count,
              i
          );


      chipView.setLayoutX(
          4
          + baseShift
          + xJitter
      );


      chipView.setLayoutY(
          stackHeight
          - CHIP_SIZE
          - i
            * STACK_OFFSET_Y
          + yJitter
      );


      chipView.setRotate(
          rotation
      );


      chipView.setEffect(
          shadow
      );


      stack
          .getChildren()
          .add(
              chipView
          );
    }


    return stack;
  }


  /*
   * Variation between entire stacks.
   */
  private void applyNaturalOffset(
      Pane stack,
      int index)
  {
    double[] verticalOffsets =
        {
          1,
          -2,
          2,
          -1,
          1,
          0,
          -1,
          2
        };


    double[] horizontalOffsets =
        {
          0,
          -2,
          1,
          2,
          -1,
          1,
          -2,
          0
        };


    double[] rotations =
        {
          -1.5,
          1.0,
          -0.5,
          1.5,
          -1.0,
          0.5,
          -0.8,
          1.2
        };


    stack.setTranslateY(
        verticalOffsets[
            index
            % verticalOffsets.length
        ]
    );


    stack.setTranslateX(
        horizontalOffsets[
            index
            % horizontalOffsets.length
        ]
    );


    stack.setRotate(
        rotations[
            index
            % rotations.length
        ]
    );
  }


  /*
   * Everything below is deterministic rather
   * than truly random.
   *
   * This means the chip pile does not visibly
   * reshuffle every time the bankroll updates.
   */
  private double getStackBaseShift(
      WagerChip chip,
      int count)
  {
    int seed =
        chip.ordinal()
        * 31
        + count
          * 17;


    double[] offsets =
        {
          -1.5,
          -0.5,
          0.0,
          0.75,
          1.5,
          -0.75
        };


    return offsets[
        Math.abs(seed)
        % offsets.length
    ];
  }


  private double getChipXJitter(
      WagerChip chip,
      int count,
      int chipIndex)
  {
    int seed =
        chip.ordinal()
        * 37
        + count
          * 19
        + chipIndex
          * 11;


    double[] offsets =
        {
          -1.2,
          -0.6,
          0.0,
          0.5,
          1.0,
          0.3,
          -0.4
        };


    return offsets[
        Math.abs(seed)
        % offsets.length
    ];
  }


  private double getChipYJitter(
      WagerChip chip,
      int count,
      int chipIndex)
  {
    int seed =
        chip.ordinal()
        * 29
        + count
          * 13
        + chipIndex
          * 7;


    double[] offsets =
        {
          0.0,
          -0.3,
          0.2,
          -0.15,
          0.1
        };


    return offsets[
        Math.abs(seed)
        % offsets.length
    ];
  }


  private double getChipRotation(
      WagerChip chip,
      int count,
      int chipIndex)
  {
    int seed =
        chip.ordinal()
        * 41
        + count
          * 23
        + chipIndex
          * 5;


    double[] rotations =
        {
          -1.2,
          -0.8,
          -0.4,
          0.0,
          0.5,
          0.9,
          1.3
        };


    return rotations[
        Math.abs(seed)
        % rotations.length
    ];
  }


  private Image loadChipImage(
      WagerChip chip)
  {
    return new Image(
        getClass()
            .getResource(
                chip.getImagePath()
            )
            .toExternalForm()
    );
  }
}