package com.breakthecore.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.breakthecore.CoreSmash;
import com.breakthecore.UserAccount;
import com.breakthecore.ui.UIComponent;

import java.util.Locale;

public class ScoresScreen extends ScreenBase {
    private Stage stage;
    private Skin skin;
    private UIScoreTable uiScoreTable;

    public ScoresScreen(CoreSmash game) {
        super(game);
        setupStage();

        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(stage);
    }

    @Override
    public void show() {
        uiScoreTable.updateTable();
    }

    @Override
    public void render(float delta) {
        stage.act();
        stage.draw();
    }

    private void setupStage() {
        stage = new Stage(gameInstance.getUIViewport());
        skin = gameInstance.getSkin();
        uiScoreTable = new UIScoreTable();


        VerticalGroup verticalGroup = new VerticalGroup();
        verticalGroup.setFillParent(true);
        verticalGroup.addActor(new Label("Scores", skin, "comic_96b"));
        verticalGroup.padTop(50);
        verticalGroup.addActor(uiScoreTable.getRoot());
        stage.addActor(verticalGroup);
    }


    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                gameInstance.setPrevScreen();
            }
            return false;
        }
    }
    private class UIScoreTable extends UIComponent {
        Label lblScore[], lblDfclty[], lblTotalScore;


         UIScoreTable(){
             Table main = new Table();
             main.defaults().padRight(30).padBottom(20);
             main.pad(50);

             main.add(new Label("A/A",skin, "comic_48"));
             main.add(new Label("Difficulty",skin, "comic_48"));
             main.add(new Label("Score",skin, "comic_48")).row();

             lblScore = new Label[5];
             lblDfclty = new Label[5];
             lblTotalScore = new Label("", skin, "comic_72bo");

             String style1st = "comic_72bo",
                     style2nd = "comic_48b",
                     styledef = "comic_48";

             for (int i = 0; i < 5; ++i) {
                 String style = i == 0 ? style1st : i == 1 ? style2nd : styledef;
                 lblScore[i] = new Label("", skin, style);
                 lblDfclty[i] = new Label("", skin, style);

                 main.add(new Label("#"+(i+1), skin, style));
                 main.add(lblDfclty[i]);
                 main.add(lblScore[i]).row();
             }

             main.add(lblTotalScore).colspan(main.getColumns()).center().padTop(100).row();

             setRoot(main);
         }

         void updateTable() {
             UserAccount user = gameInstance.getUserAccount();

             for (int i = 0; i < 5; ++i) {
                 lblScore[i].setText(String.valueOf(user.getScore(i)));
                 lblDfclty[i].setText(String.format(Locale.ENGLISH,"%.2f", user.getScoreDificulty(i)));
                 lblTotalScore.setText("Total Score: " + user.getTotalScore());
             }
         }

    }
}
